package org.jetbrains.plugins.template.toolWindow

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.execution.ProgramRunnerUtil.executeConfiguration
import com.intellij.execution.configurations.coverage.JavaCoverageEnabledConfiguration
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.compiler.CompilerMessageCategory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.rt.coverage.data.LineData
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.RenderingHints
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.swing.*

data class ParsedInitResponse(
    val analysisResponseDTO: Map<String, Any>?,
    val customDependencies: List<String>
)

data class CompleteResponseDTO(
    val generatedTestClassFqn: String,
    val generatedTestCode: String
)

class MyToolWindowFactory : ToolWindowFactory {
    // Contador de tentativas de retry
    private var retryCount: Int = 0

    private var latestGeneratedTestClassFqn: String = ""

    private lateinit var myToolWindowInstance: MyToolWindow

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        this.myToolWindowInstance = myToolWindow
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
        registrarListenerDeTeste(project)
    }

    override fun shouldBeAvailable(project: Project) = true

    inner class MyToolWindow(private val toolWindow: ToolWindow) {
        // Serialização dos resultados dos testes
        var lastTestResultsSerialized: String = ""
        private val textArea = JTextArea(10, 30).apply {
            lineWrap = true
            wrapStyleWord = true
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        lateinit var testFile: File
        var targetClassName: String = ""
        var targetClassPackage: String = ""
        var targetClassCode: String = ""
        var guidelines: String = ""
        var dependenciasCodigo: String = ""
        var parsedResponse: ParsedInitResponse? = null
        val realDependenciesUsed: MutableSet<String> = mutableSetOf()

        fun appendLog(message: String) {
            println(message)

            val closingBracketIndex = message.indexOf("]") + 1
            val rawType = if (closingBracketIndex > 0) message.substring(1, closingBracketIndex - 1).trim() else ""
            val restOfMessage = if (closingBracketIndex > 0) message.substring(closingBracketIndex) else message

            val emoji = when (rawType.uppercase()) {
                "INFO" -> "ℹ️"
                "ERROR" -> "❌"
                "PROCESSING" -> "🔄"
                "WARNING" -> "⚠️"
                else -> ""
            }

            ApplicationManager.getApplication().invokeLater {
                logsPane.append("$emoji $restOfMessage\n")
                logsPane.caretPosition = logsPane.document.length
            }
        }

        fun <T> executeWithWaitingLogs(
            tag: String,
            block: () -> T
        ): T {
            val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
            val future: ScheduledFuture<*> = scheduler.scheduleAtFixedRate(
                { appendLog("⏳[$tag] Aguardando LLM raciocinar…") },
                5, 5, TimeUnit.SECONDS
            )
            return try {
                block()
            } finally {
                future.cancel(true)
                scheduler.shutdown()
            }
        }

        fun getContent() = buildUI()

        private lateinit var logsPane: JTextArea

        private fun buildUI() = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

            val gerarTestsButton = JButton("Gerar testes unitários").apply {
                preferredSize = Dimension(200, 40)
                maximumSize = Dimension(Int.MAX_VALUE, 40)
                background = JBColor(Color(152, 251, 152), Color(152, 251, 152))
                isContentAreaFilled = true
                isOpaque = true
                foreground = JBColor.BLACK
                font = font.deriveFont(java.awt.Font.BOLD, 14f)
                alignmentX = Component.LEFT_ALIGNMENT
                border = BorderFactory.createCompoundBorder(
                    object : javax.swing.border.LineBorder(JBColor.LIGHT_GRAY, 1, true) {
                        override fun paintBorder(
                            c: Component,
                            g: java.awt.Graphics,
                            x: Int,
                            y: Int,
                            width: Int,
                            height: Int
                        ) {
                            val g2 = g.create() as java.awt.Graphics2D
                            g2.color = lineColor
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                            g2.drawRoundRect(x, y, width - 1, height - 1, 20, 20)
                            g2.dispose()
                        }
                    },
                    BorderFactory.createEmptyBorder(
                        margin.top,
                        margin.left,
                        margin.bottom,
                        margin.right
                    )
                )
                addActionListener { onGerarTestesClicked() }
            }
            add(gerarTestsButton)
            add(Box.createVerticalStrut(24))

            val monitorLabel = JBLabel("Monitor de atividades").apply {
                font = font.deriveFont(java.awt.Font.BOLD, 14f)
                alignmentX = Component.LEFT_ALIGNMENT
                border = BorderFactory.createEmptyBorder(
                    0,
                    gerarTestsButton.margin.left,
                    0,
                    gerarTestsButton.margin.right
                )
                foreground = JBColor.BLACK
            }
            add(monitorLabel)
            add(Box.createVerticalStrut(12))

            val logsPane = JTextArea().apply {
                isEditable = false
                background = JBColor(Color(0x1B1B1B), Color(0x1B1B1B))
                font = font.deriveFont(14f)
                foreground = JBColor.BLACK
                lineWrap = true
                wrapStyleWord = true
            }
            this@MyToolWindow.logsPane = logsPane
            val logsScroll = JScrollPane(logsPane).apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                preferredSize = Dimension(Short.MAX_VALUE.toInt(), 300)
                maximumSize = Dimension(Short.MAX_VALUE.toInt(), 300)
                alignmentX = Component.LEFT_ALIGNMENT
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(
                        0,
                        gerarTestsButton.margin.left,
                        0,
                        gerarTestsButton.margin.right
                    ),
                    BorderFactory.createLineBorder(JBColor.LIGHT_GRAY)
                )
            }
            add(logsScroll)

            add(Box.createVerticalGlue())

            add(Box.createVerticalStrut(12))
            val diretrizesLabel = JBLabel("Diretrizes do projeto:").apply {
                font = font.deriveFont(java.awt.Font.BOLD, 12f)
                alignmentX = Component.LEFT_ALIGNMENT
                border = BorderFactory.createEmptyBorder(
                    0,
                    gerarTestsButton.margin.left,
                    0,
                    gerarTestsButton.margin.right
                )
                foreground = JBColor.BLACK
            }
            add(diretrizesLabel)

            add(Box.createVerticalStrut(12))

            val diretrizesArea = JTextArea(4, 30).apply {
                lineWrap = true
                wrapStyleWord = true
                border = BorderFactory.createLineBorder(JBColor.LIGHT_GRAY)
                font = font.deriveFont(12f)
                foreground = JBColor.BLACK
            }

            val diretrizesScroll = JScrollPane(diretrizesArea).apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
                preferredSize = Dimension(Short.MAX_VALUE.toInt(), 100)
                maximumSize = Dimension(Short.MAX_VALUE.toInt(), 100)
                alignmentX = Component.LEFT_ALIGNMENT
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(
                        0,
                        gerarTestsButton.margin.left,
                        0,
                        gerarTestsButton.margin.right
                    ),
                    BorderFactory.createLineBorder(JBColor.WHITE)
                )
            }
            add(diretrizesScroll)
        }

        private fun onGerarTestesClicked() {
            val project = toolWindow.project
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            retryCount = 0

            if (editor != null) {
                val document = editor.document
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                appendLog("🔍 Editor identificado: ${psiFile?.virtualFile?.path}")

                if (psiFile is PsiJavaFile) {
                    if (psiFile.name.contains("Test", ignoreCase = true)) {
                        appendLog("[WARNING] Arquivo com nome 'Test' detectado. Operação cancelada para evitar autoanálise.")
                        return
                    }

                    appendLog("🔄 Iniciando geração de testes para '${psiFile.name}' no pacote '${psiFile.packageName}'.")
                    targetClassName = psiFile.name
                    targetClassPackage = psiFile.packageName
                    targetClassCode = document.text
                    // Execute init request off the UI thread to avoid blocking the EDT
                    ApplicationManager.getApplication().executeOnPooledThread {
                        enviarRequisicaoInit(project)
                    }
                } else {
                    appendLog("[INFO] O arquivo ativo não é um arquivo Java.")
                }
            } else {
                appendLog("[INFO] Nenhum editor ativo encontrado.")
            }
        }

        private fun buscarCodigoDasDependencias(project: Project, fqns: List<String>): String {
            val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)
            val scope = com.intellij.psi.search.GlobalSearchScope.allScope(project)
            val visitados = mutableSetOf<String>()
            val codigoFonte = mutableListOf<String>()
            realDependenciesUsed.clear()

            fun processarClasse(fqn: String) {
                if (fqn in visitados) {
                    appendLog("🔁 Dependência já visitada: $fqn")
                    return
                }
                appendLog("📦 Processando dependência: $fqn")
                visitados.add(fqn)
                realDependenciesUsed.add(fqn)

                val psiClass = facade.findClass(fqn, scope)
                val sourceCode = psiClass?.containingFile?.text
                if (sourceCode == null) {
                    appendLog("❌ Código não encontrado para: $fqn")
                    codigoFonte.add("// Classe não encontrada no projeto: $fqn")
                    return
                }

                appendLog("✅ Código encontrado para: $fqn")
                codigoFonte.add(sourceCode)

                val regexImport = Regex("""import\s+((com|br)\.[\w.]+);""")
                val subDependencies = regexImport.findAll(sourceCode)
                    .map { it.groupValues[1] }
                    .filter { it.startsWith("com.deckofcards") && it !in visitados }
                    .toList()

                if (subDependencies.isNotEmpty()) {
                    appendLog("🔍 Subdependências detectadas em $fqn: ${subDependencies.joinToString(", ")}")
                }

                subDependencies.forEach {
                    appendLog("➡️ Processando subdependência: $it")
                    processarClasse(it)
                }
            }

            fqns.forEach { processarClasse(it) }
            return codigoFonte.joinToString("\n\n")
        }

        private fun enviarRequisicaoInit(project: Project) {
            appendLog("🔄 Preparando payload para /init...")
            appendLog("⏳ Enviando requisição para http://localhost:8080/unitcat/api/init")

            try {
                val payload = listOf(
                    "targetClassName" to targetClassName,
                    "targetClassCode" to targetClassCode,
                    "targetClassPackage" to targetClassPackage
                ).joinToString("&") { (k, v) ->
                    "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
                }

                val client = java.net.http.HttpClient.newHttpClient()

                val request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:8080/unitcat/api/init"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
                    .build()

                val response = executeWithWaitingLogs("PROCESSING") {
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                }

                appendLog("[INFO] Código de status HTTP de /init: ${response.statusCode()}")

                val objectMapper = jacksonObjectMapper()
                val jsonTree = objectMapper.readTree(response.body())

                val analysisNode = jsonTree.get("analysisResponseDTO")
                val analysisMap: Map<String, Any>? =
                    if (analysisNode != null && !analysisNode.isNull) objectMapper.convertValue(analysisNode) else null

                val customDepsNode = jsonTree.get("customDependencies")

                val customDeps: List<String> = if (customDepsNode != null && customDepsNode.isArray) {
                    customDepsNode.mapNotNull { it.asText() }
                } else {
                    emptyList()
                }

                appendLog("✅ Dependências identificadas (${customDeps.size}): ${customDeps.joinToString(", ")}")
                appendLog("🔍 Recuperando código-fonte das dependências...")

                dependenciasCodigo = buscarCodigoDasDependencias(project, customDeps)
                appendLog("✅ Código-fonte das dependências carregado com ${customDeps.size} itens.")

                guidelines = textArea.text
                parsedResponse = ParsedInitResponse(
                    analysisResponseDTO = analysisMap,
                    customDependencies = customDeps
                )

                appendLog("📄 [INFO] Enviando dados de guidelines com tamanho de ${guidelines.length} caracteres.")
                enviarRequisicaoComplete(project)

            } catch (e: Exception) {
                appendLog("[ERROR] Erro ao enviar requisição /init: ${e.message}")
                e.printStackTrace()
            }
        }

        private fun enviarRequisicaoComplete(project: Project) {
            appendLog("[PROCESSING] Preparando payload para /complete...")
            appendLog("[PROCESSING] Enviando requisição para http://localhost:8080/unitcat/api/complete")
            appendLog("📦 Payload resumido: targetClassName='$targetClassName', targetClassPackage='$targetClassPackage', guidelines='${guidelines.take(50)}...'")

            val dependenciesName = realDependenciesUsed.joinToString(",")
            appendLog("dependenciesName = $dependenciesName")

            val completeRequestBody = listOf(
                "targetClassName" to targetClassName,
                "targetClassCode" to targetClassCode,
                "targetClassPackage" to targetClassPackage,
                "guidelines" to guidelines,
                "dependencies" to dependenciasCodigo,
                "dependenciesName" to dependenciesName
            ).joinToString("&") { (k, v) ->
                "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
            }

            appendLog("[INFO] Payload completo para /complete possui ${completeRequestBody.length} caracteres.")
            val client = java.net.http.HttpClient.newHttpClient()

            val completeRequest = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/unitcat/api/complete"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(completeRequestBody))
                .build()

            try {
                val completeResponse = executeWithWaitingLogs("PROCESSING") {
                    client.send(
                        completeRequest,
                        java.net.http.HttpResponse.BodyHandlers.ofString()
                    )
                }

                appendLog("✅ Resposta do /complete recebida com status ${completeResponse.statusCode()}")

                val objectMapper = jacksonObjectMapper()
                val completeResponseDTO = objectMapper.readValue(
                    completeResponse.body(),
                    CompleteResponseDTO::class.java
                )

                this@MyToolWindowFactory.latestGeneratedTestClassFqn = completeResponseDTO.generatedTestClassFqn
                appendLog("🎉 Classe de teste gerada: ${completeResponseDTO.generatedTestClassFqn}")

                criarClasseDeTeste(project, completeResponseDTO.generatedTestCode)

            } catch (e: Exception) {
                e.printStackTrace()
                appendLog("[ERROR] Erro ao enviar requisição /complete: ${e.message}")
            }
        }

        private fun criarClasseDeTeste(project: Project, testClassContentRaw: String) {
            appendLog("[PROCESSING] Iniciando criação da classe de teste...")

            val testClassContent = testClassContentRaw
                .removePrefix("```java")
                .removeSuffix("```")
                .trim()

            val packageLine = testClassContent.lines().firstOrNull { it.trim().startsWith("package ") }
            val packageName = packageLine?.removePrefix("package")?.removeSuffix(";")?.trim() ?: ""
            appendLog("[INFO] Pacote identificado: '$packageName'")

            val packagePath = packageName.replace('.', '/')
            val testRoot = File(project.basePath, "src/test/java/$packagePath")
            appendLog("[INFO] Diretório alvo: ${testRoot.absolutePath}")

            if (!testRoot.exists()) {
                testRoot.mkdirs()
            }

            val classNameRegex = Regex("""class\s+(\w+)""")
            val match = classNameRegex.find(testClassContent)
            val className = match?.groups?.get(1)?.value ?: "GeneratedTest"
            appendLog("[INFO] Nome da classe de teste gerada: $className")

            testFile = File(testRoot, "$className.java")
            appendLog("📁 Criando arquivo de teste: ${testFile.absolutePath}")

            testFile.writeText(testClassContent)
            appendLog("✅ Arquivo de teste criado com ${testClassContent.length} caracteres.")
            appendLog("[INFO] Arquivo salvo com sucesso.")

            val newVirtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(testFile)

            if (newVirtualFile != null) {
                appendLog("[INFO] Tentando abrir a classe de teste no editor: ${testFile.name}")
                FileEditorManager.getInstance(project).openFile(newVirtualFile, true)

                appendLog("[INFO] Classe de teste aberta com sucesso.")
            } else appendLog("[ERROR] Falha ao localizar o arquivo virtual para: ${testFile.absolutePath}")


            val fqn = if (packageName.isNotBlank()) "$packageName.$className" else className
            compileBeforeJUnit(project, fqn)
        }

        fun substituirMetodosNoArquivo(testFile: File, updatesJson: String) {
            val objectMapper = jacksonObjectMapper()

            data class ModifiedTestMethod(val methodName: String, val modifiedCode: String)
            data class UpdatePayload(
                val modifiedTestMethods: List<ModifiedTestMethod>,
                val requiredNewImports: List<String>
            )

            data class MethodBlock(
                val name: String,
                val startIndex: Int,
                val endIndex: Int,
                val originalCodeLines: List<String>
            )

            fun refactorTestClass(originalTestClasse: String, updates: UpdatePayload): String {
                val originalLines = originalTestClasse.lines()
                val newLines = mutableListOf<String>()

                var packageLine: String? = null
                val existingImports = mutableSetOf<String>()
                var importBlockEndIndex = -1
                var classStartIndex = -1

                for ((index, line) in originalLines.withIndex()) {
                    val trimmedLine = line.trim()
                    if (trimmedLine.startsWith("package ")) {
                        packageLine = line
                    } else if (trimmedLine.startsWith("import ")) {
                        existingImports.add(line)
                        importBlockEndIndex = index
                    } else if (trimmedLine.contains(" class ") && trimmedLine.endsWith("{")) {
                        if (classStartIndex == -1) {
                            classStartIndex = index
                        }
                    }
                }

                val methodBlocks = mutableListOf<MethodBlock>()
                var currentMethodStart = -1
                var currentMethodName: String? = null
                var braceLevel = 0
                var inMethod = false
                val methodSignatureRegex =
                    Regex("""^\s*(?:public|private|protected|static|\s)*\s*\w+\s+(\w+)\s*\(.*\)\s*(?:throws\s+[\w.,\s]+)?\s*\{\s*$""")
                val searchStartIndex =
                    if (importBlockEndIndex != -1) importBlockEndIndex + 1 else if (packageLine != null) 1 else 0

                for (i in searchStartIndex until originalLines.size) {
                    val line = originalLines[i]
                    val trimmedLine = line.trim()

                    if (!inMethod && trimmedLine.startsWith("@")) {
                        if (currentMethodStart == -1) {
                            currentMethodStart = i
                        }
                    }

                    val match = methodSignatureRegex.find(line)
                    if (!inMethod && match != null) {
                        val potentialName = match.groupValues[1]
                        if (currentMethodStart == -1) {
                            currentMethodStart = i
                        }
                        currentMethodName = potentialName
                        inMethod = true
                        braceLevel = line.count { it == '{' } - line.count { it == '}' }
                        if (braceLevel == 0 && line.contains("{") && line.contains("}")) {
                            methodBlocks.add(
                                MethodBlock(
                                    name = currentMethodName,
                                    startIndex = currentMethodStart,
                                    endIndex = i,
                                    originalCodeLines = originalLines.subList(currentMethodStart, i + 1)
                                )
                            )
                            inMethod = false
                            currentMethodStart = -1
                            currentMethodName = null
                        }
                    } else if (inMethod) {
                        braceLevel += line.count { it == '{' }
                        braceLevel -= line.count { it == '}' }
                        if (braceLevel <= 0) {
                            methodBlocks.add(
                                MethodBlock(
                                    name = currentMethodName ?: "<unknown>",
                                    startIndex = currentMethodStart,
                                    endIndex = i,
                                    originalCodeLines = originalLines.subList(currentMethodStart, i + 1)
                                )
                            )
                            inMethod = false
                            currentMethodStart = -1
                            currentMethodName = null
                        }
                    } else if (currentMethodStart != -1 && !trimmedLine.startsWith("@") && trimmedLine.isNotEmpty()) {
                        currentMethodStart = -1
                    }
                }

                packageLine?.let { newLines.add(it) }
                newLines.add("")
                val allImports = existingImports.toMutableSet()
                updates.requiredNewImports.forEach { imp ->
                    val importToAdd = if (imp.trim().startsWith("import ")) imp else "import ${imp.trim()};"
                    allImports.add(importToAdd)
                }
                newLines.addAll(allImports.sorted())
                newLines.add("")

                val firstContentIndex = methodBlocks.firstOrNull()?.startIndex ?: classStartIndex
                if (firstContentIndex > importBlockEndIndex + 1) {
                    newLines.addAll(originalLines.subList(importBlockEndIndex + 1, firstContentIndex))
                }

                val updatesMap = updates.modifiedTestMethods.associateBy { it.methodName }
                var lastAddedLineIndex = (methodBlocks.firstOrNull()?.startIndex ?: classStartIndex) - 1

                methodBlocks.forEach { block ->
                    if (block.startIndex > lastAddedLineIndex + 1) {
                        newLines.addAll(originalLines.subList(lastAddedLineIndex + 1, block.startIndex))
                    }

                    val update = updatesMap[block.name]

                    if (update != null) {
                        newLines.addAll(update.modifiedCode.lines())
                        appendLog("[PROCESSING] Método '${block.name}' substituído com sucesso.")
                    } else {
                        newLines.addAll(block.originalCodeLines)
                    }

                    lastAddedLineIndex = block.endIndex
                }

                if (lastAddedLineIndex < originalLines.size - 1) {
                    newLines.addAll(originalLines.subList(lastAddedLineIndex + 1, originalLines.size))
                }

                return newLines.joinToString("\n")
            }

            try {
                val updates = objectMapper.readValue<UpdatePayload>(updatesJson)

                val original = testFile.readText()
                appendLog("[INFO] Tamanho do arquivo original para refatoração: ${original.length} caracteres.")

                val updated = refactorTestClass(original, updates)
                testFile.writeText(updated)

                appendLog("[INFO] Tamanho do arquivo refatorado: ${updated.length} caracteres.")
                appendLog("[INFO] Classe Java atualizada com sucesso com base no JSON do /retry.")

            } catch (e: Exception) {
                appendLog("[ERROR] Erro ao processar JSON de atualização: ${e.message}")
                e.printStackTrace()
            }
        }

    }

    // Variáveis para contagem de testes
    private var totalTests = 0
    private var passedTests = 0
    private var errorTests = 0
    private lateinit var coverageReport: Map<String, Any>

    private fun registrarListenerDeTeste(project: Project) {
        val connection: MessageBusConnection = project.messageBus.connect(project as Disposable)
        connection.subscribe(SMTRunnerEventsListener.TEST_STATUS, object : SMTRunnerEventsListener {
            override fun onTestingStarted(p0: SMTestProxy.SMRootTestProxy) {
                totalTests = 0
                passedTests = 0
                errosDeTesteGlobal.clear()
                errorTests = 0
            }

            override fun onTestingFinished(root: SMTestProxy.SMRootTestProxy) {
                coverageReport = capturarCoverage(project)
                myToolWindowInstance.appendLog("[INFO] Coverage Report capturado: $coverageReport")

                val testResults = mapOf(
                    "totalTests" to totalTests,
                    "passedTests" to passedTests,
                    "failedTests" to errosDeTesteGlobal.size,
                    "errorTests" to errorTests
                )

                val objectMapper = jacksonObjectMapper()
                val serializedTestResults = objectMapper.writeValueAsString(testResults)
                myToolWindowInstance.appendLog("[INFO] test_results = $serializedTestResults")
                myToolWindowInstance.lastTestResultsSerialized = serializedTestResults

                if (errosDeTesteGlobal.isNotEmpty()) {
                    myToolWindowInstance.appendLog("⚠️ Testes falharam; iniciando retry para ajustar os métodos de teste.")
                    processRetry(project)
                } else {
                    retryCount = 0;
                    myToolWindowInstance.appendLog("[INFO] retryCount resetado para 0 após execução bem-sucedida.")
                }
            }

            override fun onTestsCountInSuite(p0: Int) {}

            override fun onTestStarted(p0: SMTestProxy) {
                totalTests++
            }
            override fun onTestIgnored(p0: SMTestProxy) {}
            override fun onSuiteFinished(p0: SMTestProxy) {}
            override fun onSuiteStarted(p0: SMTestProxy) {}
            override fun onCustomProgressTestsCategory(p0: String?, p1: Int) {}
            override fun onCustomProgressTestStarted() {}
            override fun onCustomProgressTestFailed() {}
            override fun onCustomProgressTestFinished() {}
            override fun onSuiteTreeNodeAdded(p0: SMTestProxy) {}
            override fun onSuiteTreeStarted(p0: SMTestProxy) {}

            override fun onTestFinished(testProxy: SMTestProxy) {
                // Se não for defeito, conta como passed
                if (!testProxy.isDefect) {
                    passedTests++
                }
            }

            override fun onTestFailed(testProxy: SMTestProxy) {
                myToolWindowInstance.appendLog("❌ Teste falhou: '${testProxy.name}'. Capturando detalhes...")

                val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return

                ApplicationManager.getApplication().runReadAction {
                    val location = testProxy.getLocation(
                        project,
                        com.intellij.psi.search.GlobalSearchScope.projectScope(project)
                    )

                    val psiElement = location?.psiElement
                    val psiMethod = PsiTreeUtil.getContextOfType(
                        psiElement,
                        PsiMethod::class.java,
                        false
                    )

                    val nomeMetodoReal = psiMethod?.name ?: testProxy.name
                    val erro = mapOf(
                        "method_name" to nomeMetodoReal,
                        "error_message" to (testProxy.errorMessage ?: ""),
                        "stack_trace" to (testProxy.stacktrace ?: "")
                    )

                    myToolWindowInstance.appendLog("🔎 Detalhes do erro no método '$nomeMetodoReal': ${testProxy.errorMessage}")
                    errosDeTesteGlobal.add(erro)
                }
                // Se for erro (por exceção), soma errorTests
                if (testProxy.errorMessage?.contains("Exception") == true) {
                    errorTests++
                }
            }
        })
    }

    private fun processRetry(project: Project) {
        if (retryCount >= 3) {
            myToolWindowInstance.appendLog("🚫 Limite de tentativas de retry atingido (3). Processo encerrado.")
            return
        }

        myToolWindowInstance.appendLog("[INFO] Iniciando processo de retry em background...")
        // Run coverage capture, HTTP request and file updates on a pooled thread
        ApplicationManager.getApplication().executeOnPooledThread {
            val formParams = listOf(
                "targetClassName" to myToolWindowInstance.targetClassName,
                "targetClassPackage" to myToolWindowInstance.targetClassPackage,
                "targetClassCode" to myToolWindowInstance.targetClassCode,
                "testClassCode" to myToolWindowInstance.testFile.readText(),
                "dependencies" to myToolWindowInstance.dependenciasCodigo,
                "dependenciesName" to myToolWindowInstance.realDependenciesUsed.joinToString(","),
                "failingTestDetailsRequestDTOS" to jacksonObjectMapper().writeValueAsString(errosDeTesteGlobal),
                "testResults" to myToolWindowInstance.lastTestResultsSerialized,
                "coverageReport" to jacksonObjectMapper().writeValueAsString(coverageReport),
                "attemptNumber" to retryCount.toString()
            ).joinToString("&") { (k, v) ->
                "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
            }

            myToolWindowInstance.appendLog("[INFO] Enviando detalhes dos testes falhos para ajustá-los via API")
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/unitcat/api/retry"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formParams))
                .build()

            try {
                // Send HTTP request off the UI thread
                val response = myToolWindowInstance.executeWithWaitingLogs("PROCESSING") {
                    java.net.http.HttpClient.newHttpClient()
                        .send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                }

                // Update UI and write file on the UI thread
                ApplicationManager.getApplication().runWriteAction {
                    myToolWindowInstance.appendLog("✅ Resposta do /retry recebida: ${response.body()}")
                    try {
                        val testFile = myToolWindowInstance.testFile
                        myToolWindowInstance.substituirMetodosNoArquivo(testFile, response.body())
                    } catch (e: Exception) {
                        myToolWindowInstance.appendLog("[ERROR] Falha ao substituir métodos no arquivo: ${e.message}")
                    }
                    // Clear state and increment retry count
                    errosDeTesteGlobal.clear()
                    totalTests = 0
                    passedTests = 0
                    errorTests = 0

                    retryCount++

                    myToolWindowInstance.appendLog("[INFO] Retry #$retryCount concluído.")
                }

                // Continue with compilation and re-run tests in background
                compileBeforeJUnit(project, latestGeneratedTestClassFqn)

            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    myToolWindowInstance.appendLog("[ERROR] Erro durante retry: ${e.message}")
                }
            }
        }
    }

    private fun capturarCoverage(project: Project): Map<String, Any> {
        val coverageDataManager = com.intellij.coverage.CoverageDataManager.getInstance(project)
        val currentSuite = coverageDataManager.currentSuitesBundle ?: run {
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: currentSuitesBundle está nulo.")
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: Motivo do retorno vazio — cobertura indisponível.")
            return emptyMap()
        }
        val projectData = currentSuite.coverageData ?: run {
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: coverageData está nulo.")
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: Motivo do retorno vazio — cobertura indisponível.")
            return emptyMap()
        }

        val fqn = myToolWindowInstance.targetClassPackage + "." + myToolWindowInstance.targetClassName.removeSuffix(".java")
        val classData = projectData.getClassData(fqn) ?: run {
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: classData está nulo para $fqn.")
            myToolWindowInstance.appendLog("[WARNING] capturarCoverage: Motivo do retorno vazio — cobertura indisponível.")
            return emptyMap()
        }

        val lines = classData.lines
        myToolWindowInstance.appendLog("[DEBUG] lines?.size = ${lines?.size}")

        val totalLines = lines?.count { it != null } ?: 0
        val coveredLines = lines?.count {
            val line = it as? LineData
            line != null && line.hits > 0
        } ?: 0

        val linesMissed = lines?.mapIndexedNotNull { index, rawLine ->
            val line = rawLine as? LineData
            if (line != null) {
                myToolWindowInstance.appendLog("[DEBUG] Linha $index - hits: ${line.hits}")
            }
            if (line != null && line.hits == 0) {
                mapOf("line" to index, "reason" to "Linha não executada pelos testes")
            } else null
        } ?: emptyList()

        val percentage = if (totalLines > 0) (coveredLines * 100.0 / totalLines) else 0.0

        myToolWindowInstance.appendLog("[INFO] capturarCoverage: FQN=$fqn, totalLines=$totalLines, coveredLines=$coveredLines, percentage=$percentage")

        return mapOf(
            "classFqn" to fqn,
            "linesTotal" to totalLines,
            "linesCovered" to coveredLines,
            "linesMissed" to linesMissed,
            "coveragePercentage" to percentage
        )
    }

    private fun compileBeforeJUnit(project: Project, fqn: String) {
        myToolWindowInstance.appendLog("[INFO] compileBeforeJUnit invocado para fqn='$fqn'")
        val lastDot = fqn.lastIndexOf('.')

        val packageName = if (lastDot != -1) fqn.substring(0, lastDot) else ""
        val className = if (lastDot != -1) fqn.substring(lastDot + 1) else fqn

        myToolWindowInstance.appendLog("[INFO] Iniciando compilação para $packageName.$className...")
        val compileScope = CompilerManager.getInstance(project).createProjectCompileScope(project)

        ApplicationManager.getApplication().invokeLater {
            CompilerManager.getInstance(project).make(compileScope) { aborted, errorsCount, _, compileContext ->
                if (aborted || errorsCount > 0) {
                    myToolWindowInstance.appendLog("[INFO] Erros de compilação detectados: $errorsCount. Coletando mensagens.")

                    compileContext.getMessages(CompilerMessageCategory.ERROR).forEach { msg ->
                        myToolWindowInstance.appendLog("[INFO] Messagem de Erro de Compilação recebida: ${msg.message}")
                        myToolWindowInstance.appendLog("[INFO] VirtualFile path: ${msg.virtualFile.path}")

                        val vFile = msg.virtualFile
                        val document = FileDocumentManager.getInstance().getDocument(vFile)

                        val lineNum = try {
                            val ln = msg.javaClass.getMethod("getLineNumber").invoke(msg) as? Int
                            ln?.takeIf { it >= 1 } ?: (
                                try { msg.javaClass.getMethod("getLine").invoke(msg) as? Int } catch (_: Exception) { null }
                            )?.takeIf { it >= 1 } ?: 1

                        } catch (_: Exception) {
                            try {
                                val ln = msg.javaClass.getMethod("getLine").invoke(msg) as? Int
                                ln?.takeIf { it >= 1 } ?: 1
                            } catch (_: Exception) {
                                1
                            }
                        }

                        myToolWindowInstance.appendLog("[INFO] Determinado número da linha: $lineNum")
                        val offset = document?.getLineStartOffset(lineNum - 1) ?: 0

                        myToolWindowInstance.appendLog("[INFO] Calculado offset para erro: $offset")
                        val psiFile = PsiManager.getInstance(project).findFile(vFile)
                        val psiElement = psiFile?.findElementAt(offset)

                        val psiMethod = PsiTreeUtil.getParentOfType(
                            psiElement, PsiMethod::class.java, false
                        )

                        myToolWindowInstance.appendLog("[INFO] Resolved psiMethod: ${psiMethod?.name}")
                        val methodName = psiMethod?.name
                            ?: vFile.name.substringBeforeLast(".java")

                        val errorMap = mapOf(
                            "methodName" to methodName,
                            "errorMessage" to msg.message,
                            "stackTrace" to msg.message
                        )

                        myToolWindowInstance.appendLog("[INFO] Adicionando erroMap: method_name=$methodName")
                        errosDeTesteGlobal.add(errorMap)
                    }

                    processRetry(project)
                } else {
                    myToolWindowInstance.appendLog("[INFO] Compilação bem-sucedida para $packageName.$className. Executando JUnit Runner.")
                    executeJUnitRunner(project, latestGeneratedTestClassFqn)
                }
            }
        }
    }

    private fun executeJUnitRunner(project: Project, generatedTestClassFqn: String) {
        myToolWindowInstance.appendLog("▶️ Executando testes JUnit para '$generatedTestClassFqn'...")

        // Compute fully qualified name of the class under test (remove .java suffix if present)
        val classUnderTestName = myToolWindowInstance.targetClassName.removeSuffix(".java")
        val classUnderTestFqn = "${myToolWindowInstance.targetClassPackage}.$classUnderTestName"

        val lastDot = generatedTestClassFqn.lastIndexOf('.')
        val packageName = if (lastDot != -1) generatedTestClassFqn.substring(0, lastDot) else ""
        val className = if (lastDot != -1) generatedTestClassFqn.substring(lastDot + 1) else generatedTestClassFqn

        myToolWindowInstance.appendLog("[INFO] Convertido packageName='$packageName', className='$className'")
        myToolWindowInstance.appendLog("[INFO] Agendando execução na pooled thread para $packageName.$className")

        ApplicationManager.getApplication().executeOnPooledThread {
            myToolWindowInstance.appendLog("[INFO] Resolvendo PsiClass para '$packageName.$className' na pooled thread")

            val psiClass = ApplicationManager.getApplication().runReadAction<com.intellij.psi.PsiClass?> {
                com.intellij.psi.JavaPsiFacade.getInstance(project)
                    .findClass(
                        if (packageName.isNotBlank()) "$packageName.$className" else className,
                        com.intellij.psi.search.GlobalSearchScope.projectScope(project)
                    )
            }

            if (psiClass != null) {
                ApplicationManager.getApplication().invokeLater {
                    myToolWindowInstance.appendLog("[INFO] PsiClass resolvido: '${psiClass.name}'. Preparando JUnit run configuration.")

                    val runManager = com.intellij.execution.RunManager.getInstance(project)
                    val configurationFactory = com.intellij.execution.junit.JUnitConfigurationType
                        .getInstance().configurationFactories[0]

                    val settings = runManager.createConfiguration("$className [UniCat]", configurationFactory)
                    val configuration = settings.configuration as com.intellij.execution.junit.JUnitConfiguration

                    val rawCoverageConfig = JavaCoverageEnabledConfiguration.getFrom(configuration)
                    if (rawCoverageConfig != null) {
                        rawCoverageConfig.coveragePatterns = arrayOf(
                            com.intellij.ui.classFilter.ClassFilter(classUnderTestFqn)
                        )
                    } else {
                        myToolWindowInstance.appendLog("[WARNING] Não foi possível configurar o padrão de cobertura. A configuração não é do tipo JavaCoverageEnabledConfiguration.")
                    }

                    configuration.setMainClass(psiClass)
                    configuration.setModule(ModuleManager.getInstance(project).modules.firstOrNull())

                    runManager.addConfiguration(settings)
                    runManager.selectedConfiguration = settings

                    val executor = com.intellij.execution.ExecutorRegistry.getInstance().getExecutorById("Coverage")
                    if (executor != null) {
                        executeConfiguration(settings, executor)
                    } else {
                        myToolWindowInstance.appendLog("[ERROR] Executor 'Coverage' não encontrado.")
                    }

                    myToolWindowInstance.appendLog("🎉 Compilação realizada com sucesso para '$generatedTestClassFqn'.")
                }
            } else {
                myToolWindowInstance.appendLog("[ERROR] Não foi possível localizar PsiClass para '$packageName.$className'.")
            }
        }
    }

    companion object {
        @JvmStatic
        val errosDeTesteGlobal: MutableList<Map<String, String>> = mutableListOf()
    }
}