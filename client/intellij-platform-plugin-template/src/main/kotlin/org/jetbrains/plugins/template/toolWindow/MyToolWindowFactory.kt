package org.jetbrains.plugins.template.toolWindow

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.execution.ProgramRunnerUtil.executeConfiguration
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
import javax.swing.*
import javax.swing.text.DefaultEditorKit
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

data class ParsedInitResponse(
    val analysisResponseDTO: Map<String, Any>?,
    val customDependencies: List<String>
)

data class CompleteResponseDTO(
    val generatedTestClassFqn: String,
    val generatedTestCode: String
)

class MyToolWindowFactory : ToolWindowFactory {

    // Store the FQN of the latest generated test class for retry cycles
    private var latestGeneratedTestClassFqn: String = ""

    // Hold a reference to the tool window instance
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

        fun appendLog(message: String) {
            println(message)

            ApplicationManager.getApplication().invokeLater {
                val paneField = this::class.java.getDeclaredField("logsPane")
                paneField.isAccessible = true
                val logsPaneInstance = paneField.get(this) as JTextPane
                val doc = logsPaneInstance.styledDocument as StyledDocument

                val closingBracketIndex = message.indexOf("]") + 1
                val rawType = if (closingBracketIndex > 0) message.substring(1, closingBracketIndex - 1).trim() else ""
                val restOfMessage = if (closingBracketIndex > 0) message.substring(closingBracketIndex) else message

                val fieldWidth = 10

                val defaultAttr = SimpleAttributeSet().also {
                    StyleConstants.setForeground(it, JBColor.BLACK)
                }
                val typeAttr = SimpleAttributeSet().also {
                    when {
                        rawType.equals("INFO", ignoreCase = true) -> StyleConstants.setForeground(it, JBColor.BLUE)
                        rawType.equals("ERROR", ignoreCase = true) -> StyleConstants.setForeground(it, JBColor.RED)
                        rawType.equals("PROCESSING", ignoreCase = true) -> StyleConstants.setForeground(it, JBColor.YELLOW)
                        else -> StyleConstants.setForeground(it, JBColor.WHITE)
                    }
                }

                doc.insertString(doc.length, "[", defaultAttr)
                doc.insertString(doc.length, rawType, typeAttr)
                val paddingCount = (fieldWidth - rawType.length).coerceAtLeast(0)
                val padding = " ".repeat(paddingCount)
                doc.insertString(doc.length, "$padding]", defaultAttr)
                doc.insertString(doc.length, " $restOfMessage\n", defaultAttr)

                logsPaneInstance.caretPosition = doc.length
            }
        }

        fun getContent() = buildUI()

        private lateinit var logsPane: JTextPane

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

            val logsPane = JTextPane().apply {
                isEditable = false
                background = JBColor(Color(0x1B1B1B), Color(0x1B1B1B))
                caretColor = JBColor.BLACK
                styledDocument.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n")
                this.font = this.font.deriveFont(14f)
                this.foreground = JBColor.BLACK
            }
            this@MyToolWindow.logsPane = logsPane
            val logsScroll = JScrollPane(logsPane).apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
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
            if (editor != null) {
                val document = editor.document
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                appendLog("[INFO] Editor identificado. Arquivo ativo: ${psiFile?.virtualFile?.path}")
                if (psiFile is PsiJavaFile) {
                    if (psiFile.name.contains("Test", ignoreCase = true)) {
                        appendLog("[WARNING] Arquivo com nome 'Test' detectado. Operação cancelada para evitar autoanálise.")
                        return
                    }
                    appendLog("[INFO] Iniciando análise da classe '${psiFile.name}' para geração de testes.")
                    targetClassName = psiFile.name
                    targetClassPackage = psiFile.packageName
                    targetClassCode = document.text
                    enviarRequisicaoInit(project)
                } else {
                    appendLog("[INFO] O arquivo ativo não é um arquivo Java.")
                }
            } else {
                appendLog("[INFO] Nenhum editor ativo encontrado.")
            }
        }

        private fun buscarCodigoDasDependencias(
            project: Project,
            fqns: List<String>
        ): String {
            val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)
            val scope = com.intellij.psi.search.GlobalSearchScope.allScope(project)
            return fqns.joinToString("\n\n") { qualifiedName ->
                val psiClass = facade.findClass(qualifiedName, scope)
                psiClass?.containingFile?.text
                    ?: "// Classe não encontrada no projeto: $qualifiedName"
            }
        }

        private fun enviarRequisicaoInit(project: Project) {
            appendLog("[PROCESSING] Classe alvo: $targetClassName (pacote: $targetClassPackage)")
            appendLog("[PROCESSING] Enviando requisição /init...")

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
                val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                appendLog("[INFO] Código de status HTTP de /init: ${response.statusCode()}")
                val objectMapper = jacksonObjectMapper()
                val jsonTree = objectMapper.readTree(response.body())
                // Parse analysisResponseDTO and customDependencies
                val analysisNode = jsonTree.get("analysisResponseDTO")
                val analysisMap: Map<String, Any>? =
                    if (analysisNode != null && !analysisNode.isNull) objectMapper.convertValue(analysisNode) else null
                val customDepsNode = jsonTree.get("customDependencies")
                val customDeps: List<String> = if (customDepsNode != null && customDepsNode.isArray) {
                    customDepsNode.mapNotNull { it.asText() }
                } else {
                    emptyList()
                }
                appendLog("[INFO] Dependências vindas do /init (${customDeps.size}): ${customDeps.joinToString(", ")}")
                dependenciasCodigo = buscarCodigoDasDependencias(project, customDeps)
                appendLog("[INFO] Códigos das dependências localizadas.")
                appendLog("[INFO] Conteúdo das dependências localizadas:\n$dependenciasCodigo")
                guidelines = textArea.text
                parsedResponse = ParsedInitResponse(
                    analysisResponseDTO = analysisMap,
                    customDependencies = customDeps
                )
                appendLog("[INFO] Enviando dados de guidelines com tamanho ${guidelines.length} caracteres.")
                enviarRequisicaoComplete(project)
            } catch (e: Exception) {
                appendLog("[ERROR] Erro ao enviar requisição /init: ${e.message}")
                e.printStackTrace()
            }
        }

        // Removido: obterDependenciasDoPsi, pois dependências agora vêm do endpoint /init

        private fun enviarRequisicaoComplete(project: Project) {
            appendLog("[PROCESSING] Enviando requisição /complete...")
            appendLog("[PROCESSING] Payload enviado:")
            appendLog("targetClassName = $targetClassName")
            appendLog("targetClassCode (truncado) = ${targetClassCode.take(100)}...")
            appendLog("targetClassPackage = $targetClassPackage")
            appendLog("guidelines = ${guidelines.take(100)}...")
            val dependenciesName = parsedResponse?.customDependencies?.joinToString(",") ?: ""
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
                val completeResponse = client.send(
                    completeRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
                )
                appendLog("[INFO] Código de status HTTP de /complete: ${completeResponse.statusCode()}")
                val objectMapper = jacksonObjectMapper()
                val completeResponseDTO = objectMapper.readValue(
                    completeResponse.body(),
                    CompleteResponseDTO::class.java
                )
                // Store FQN for retries
                this@MyToolWindowFactory.latestGeneratedTestClassFqn = completeResponseDTO.generatedTestClassFqn
                appendLog("[INFO] Resposta /complete recebida.")
                appendLog("[INFO] Classe gerada: ${completeResponseDTO.generatedTestClassFqn}")
                appendLog(
                    "[INFO] Código gerado (preview):\n${
                        completeResponseDTO.generatedTestCode.lines().take(10).joinToString("\n")
                    }"
                )

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
            appendLog("[INFO] Salvando classe de teste em: ${testFile.absolutePath}")
            testFile.writeText(testClassContent)
            appendLog("[INFO] Conteúdo da nova classe de teste possui ${testClassContent.length} caracteres.")
            appendLog("[INFO] Arquivo salvo com sucesso.")

            val newVirtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(testFile)
            if (newVirtualFile != null) {
                appendLog("[INFO] Tentando abrir a classe de teste no editor: ${testFile.name}")
                FileEditorManager.getInstance(project).openFile(newVirtualFile, true)
                appendLog("[INFO] Classe de teste aberta com sucesso.")
            } else {
                appendLog("[ERROR] Falha ao localizar o arquivo virtual para: ${testFile.absolutePath}")
            }

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

//        private fun executarGoalMaven(
//            project: Project,
//            retryCount: Int = 0,
//            goal: String = "test"
//        ) {
//            val mavenProjects = org.jetbrains.idea.maven.project.MavenProjectsManager.getInstance(project).projects
//            appendLog("[INFO] MavenProjectsManager encontrou ${mavenProjects.size} projetos")
//            val mavenProject = mavenProjects.firstOrNull()
//            if (mavenProject == null) {
//                appendLog("[ERROR] Não há projetos Maven registrados ainda; pulando execução")
//                return
//            }
//
//            val projectDirPath = mavenProject.directory
//            appendLog("[INFO] Diretório do projeto Maven: $projectDirPath")
//
//            val parameters = MavenRunnerParameters(
//                true,
//                projectDirPath,
//                null as String?,
//                listOf(goal),
//                emptyList()
//            )
//
//            val settings = MavenRunnerSettings()
//            val generalSettings: MavenGeneralSettings? = null
//
//            val outputStream = ByteArrayOutputStream()
//            val processListener = object : ProcessAdapter() {
//                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
//                    super.onTextAvailable(event, outputType)
//                    print("[MAVEN] ${event.text.trim()}")
//                    outputStream.write(event.text.toByteArray())
//                }
//
//                override fun processTerminated(event: ProcessEvent) {
//                    super.processTerminated(event)
//                    appendLog("[PROCESSING] Iniciando execução do Maven com retryCount = $retryCount")
//                    if (retryCount >= 5) {
//                        appendLog("[PROCESSING] Limite de 5 tentativas de retry atingido.")
//                        return
//                    }
//                    val logContent = outputStream.toString(Charsets.UTF_8.name())
//                    appendLog("[INFO] Logs Maven capturados.")
//                    appendLog("[INFO] Conteúdo completo do log Maven: \n$logContent")
//
//                    val errosDeTesteFinal = MyToolWindowFactory.errosDeTesteGlobal
//                    appendLog("📋 [INFO] Lista final de testes com falha:")
//
//                    errosDeTesteFinal.forEachIndexed { index, erro ->
//                        appendLog("🔹 Erro ${index + 1}:")
//                        appendLog("   Método real: ${erro["nomeMetodo"]}")
//                        appendLog("   DisplayName: ${erro["displayName"]}")
//                        appendLog("   Mensagem de erro: ${erro["mensagemErro"]?.take(300)}")
//                        appendLog("   Stacktrace: ${erro["stacktrace"]?.take(300)}")
//                        appendLog("   Código do método:\n${erro["codigoMetodo"]?.take(1000)}")
//                        appendLog("----------------------------------------------------")
//                    }
//
//                    val objectMapper = jacksonObjectMapper()
//                    appendLog("[INFO] Estrutura de dados completa dos testes com falha:")
//                    appendLog(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errosDeTesteFinal))
//
//                    appendLog("[PROCESSING] Enviando requisição /retry com ${errosDeTesteFinal.size} testes falhos...")
//                    appendLog("[PROCESSING] Construindo corpo da requisição /retry...")
//
//                    val testClassCodeAtual: String = try {
//                        val vFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
//                            .findFileByIoFile(testFile)
//                        if (vFile != null) {
//                            val doc = FileDocumentManager.getInstance().getDocument(vFile)
//                            doc?.text ?: testFile.readText()
//                        } else {
//                            testFile.readText()
//                        }
//                    } catch (_: Exception) {
//                        testFile.readText()
//                    }
//
//                    val dependenciesNameRetry = parsedResponse?.customDependencies?.joinToString(",") ?: ""
//                    val formParams = mutableListOf(
//                        "targetClassName" to targetClassName,
//                        "targetClassPackage" to targetClassPackage,
//                        "targetClassCode" to targetClassCode,
//                        "testClassCode" to testClassCodeAtual,
//                        "dependencies" to dependenciasCodigo,
//                        "dependenciesName" to dependenciesNameRetry
//                    )
//                    val failingTestsJson = objectMapper.writeValueAsString(errosDeTesteFinal)
//                    formParams.add("failingTestDetailsRequestDTOS" to failingTestsJson)
//
//                    val retryRequestBody = formParams.joinToString("&") { (key, value) ->
//                        "${java.net.URLEncoder.encode(key, "UTF-8")}=${java.net.URLEncoder.encode(value, "UTF-8")}"
//                    }
//
//                    appendLog("[INFO] Processo Maven finalizado (código: ${event.exitCode})")
//
//                    val errorLines = logContent.lines()
//                        .filter { it.contains("[ERROR]") || it.contains("COMPILATION ERROR") || it.contains("BUILD FAILURE") }
//                        .joinToString("\n")
//
//                    if (errorLines.isNotEmpty()) {
//                        appendLog("[ERROR] Foram encontrados erros '[ERROR]' no log Maven.")
//                        appendLog("[PROCESSING] Enviando requisição HTTP para /retry...")
//                        val retryRequest = java.net.http.HttpRequest.newBuilder()
//                            .uri(java.net.URI.create("http://localhost:8080/unitcat/api/retry"))
//                            .header("Content-Type", "application/x-www-form-urlencoded")
//                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(retryRequestBody))
//                            .build()
//                        try {
//                            val retryResponse = java.net.http.HttpClient.newHttpClient().send(
//                                retryRequest,
//                                java.net.http.HttpResponse.BodyHandlers.ofString()
//                            )
//                            appendLog("[INFO] Resposta /retry recebida:")
//                            appendLog("[INFO] Conteúdo:\n${retryResponse.body()}")
//
//                            val responseJson = retryResponse.body()
//                            val retryResultNode = objectMapper.readTree(responseJson)
//                            appendLog("[PROCESSING] Resposta JSON de /retry parseada com sucesso.")
//                            substituirMetodosNoArquivo(testFile, retryResponse.body())
//                            appendLog("[INFO] Classe de teste atualizada com métodos do /retry.")
//                            executarGoalMaven(project, retryCount + 1)
//                            val modifiedMethods = retryResultNode.get("modifiedTestMethods")
//                            val requiredImports = retryResultNode.get("requiredNewImports")
//                            appendLog("[PROCESSING] Quantidade de métodos modificados recebidos: ${modifiedMethods?.size() ?: 0}")
//                            appendLog("[PROCESSING] Quantidade de novos imports recebidos: ${requiredImports?.size() ?: 0}")
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                            appendLog("[ERROR] Falha ao chamar o endpoint /retry: ${e.message}")
//                        }
//                    } else {
//                        appendLog("[INFO] Nenhum erro '[ERROR]' identificado no log Maven.")
//                        appendLog("[INFO] Build Maven bem-sucedido, sem falhas nos testes.")
//                    }
//                }
//            }
//
//            ApplicationManager.getApplication().invokeAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
//
//            MavenRunConfigurationType.runConfiguration(
//                project,
//                parameters,
//                generalSettings,
//                settings,
//                { descriptor ->
//                    val handler = descriptor.processHandler
//                    if (handler != null) {
//                        handler.addProcessListener(processListener)
//                    } else {
//                        appendLog("[ERROR] Erro: ProcessHandler é nulo após o início do processo.")
//                    }
//                },
//                false
//            )
//        }

    }

    private fun registrarListenerDeTeste(project: Project) {
        val connection: MessageBusConnection = project.messageBus.connect(project as Disposable)
        connection.subscribe(SMTRunnerEventsListener.TEST_STATUS, object : SMTRunnerEventsListener {
            override fun onTestingStarted(p0: SMTestProxy.SMRootTestProxy) {}
            override fun onTestingFinished(root: SMTestProxy.SMRootTestProxy) {
                if (errosDeTesteGlobal.isNotEmpty()) {
                    myToolWindowInstance.appendLog("[INFO] Testes finalizados com falhas. Iniciando retry via API.")
                    processRetry(project)
                }
            }
            override fun onTestsCountInSuite(p0: Int) {}
            override fun onTestStarted(p0: SMTestProxy) {}
            override fun onTestIgnored(p0: SMTestProxy) {}
            override fun onSuiteFinished(p0: SMTestProxy) {}
            override fun onSuiteStarted(p0: SMTestProxy) {}
            override fun onCustomProgressTestsCategory(p0: String?, p1: Int) {}
            override fun onCustomProgressTestStarted() {}
            override fun onCustomProgressTestFailed() {}
            override fun onCustomProgressTestFinished() {}
            override fun onSuiteTreeNodeAdded(p0: SMTestProxy) {}
            override fun onSuiteTreeStarted(p0: SMTestProxy) {}
            override fun onTestFinished(testProxy: SMTestProxy) {}
            override fun onTestFailed(testProxy: SMTestProxy) {
                myToolWindowInstance.appendLog("[INFO] Teste com falha encontrada.")

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

                    myToolWindowInstance.appendLog("[INFO] Detalhes do teste com falha: $erro")
                    errosDeTesteGlobal.add(erro)
                }
            }
        })
    }

    private fun processRetry(project: Project) {
        val objectMapper = jacksonObjectMapper()
        val failingTestsJson = objectMapper.writeValueAsString(errosDeTesteGlobal)
        val formParams = listOf(
            "targetClassName" to myToolWindowInstance.targetClassName,
            "targetClassPackage" to myToolWindowInstance.targetClassPackage,
            "targetClassCode" to myToolWindowInstance.targetClassCode,
            "testClassCode" to myToolWindowInstance.testFile.readText(),
            "dependencies" to myToolWindowInstance.dependenciasCodigo,
            "dependenciesName" to (myToolWindowInstance.parsedResponse?.customDependencies?.joinToString(",") ?: ""),
            "failingTestDetailsRequestDTOS" to failingTestsJson
        ).joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        try {
            val request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/unitcat/api/retry"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(formParams))
                .build()
            val response = java.net.http.HttpClient.newHttpClient()
                .send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
            myToolWindowInstance.appendLog("[INFO] Resposta /retry: ${response.body()}")
            // substituirMetodosNoArquivo call
            try {
                val testFile = myToolWindowInstance.testFile
                myToolWindowInstance.substituirMetodosNoArquivo(testFile, response.body())
//                // Refresh VFS and PSI to pick up updated testFile
//                val vFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
//                    .refreshAndFindFileByIoFile(testFile)
//                vFile?.refresh(false, false)
//                val document = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
//                    .getDocument(vFile!!)
//                if (document != null) {
//                    com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
//                        .reloadFromDisk(document)
//                }
            } catch (e: Exception) {
                myToolWindowInstance.appendLog("[ERROR] Falha ao substituir métodos no arquivo: ${e.message}")
            }
            errosDeTesteGlobal.clear()
            // Schedule compilation on EDT
            ApplicationManager.getApplication().invokeLater {
                compileBeforeJUnit(project, latestGeneratedTestClassFqn)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            myToolWindowInstance.appendLog("[ERROR] Falha no retry: ${e.message}")
        }
    }

    private fun compileBeforeJUnit(project: Project, fqn: String) {
        myToolWindowInstance.appendLog("[DEBUG] compileBeforeJUnit called with fqn='$fqn'")
        val lastDot = fqn.lastIndexOf('.')
        val packageName = if (lastDot != -1) fqn.substring(0, lastDot) else ""
        val className = if (lastDot != -1) fqn.substring(lastDot + 1) else fqn
        myToolWindowInstance.appendLog("[DEBUG] Iniciando compilação para $packageName.$className...")
        val compileScope = CompilerManager.getInstance(project).createProjectCompileScope(project)
        ApplicationManager.getApplication().invokeLater {
            CompilerManager.getInstance(project).make(compileScope) { aborted, errorsCount, _, compileContext ->
                if (aborted || errorsCount > 0) {
                    myToolWindowInstance.appendLog("[INFO] Erros de compilação detectados: $errorsCount. Coletando mensagens.")
                    compileContext.getMessages(CompilerMessageCategory.ERROR).forEach { msg ->
                        myToolWindowInstance.appendLog("[DEBUG] Compilation error message received: ${msg.message}")
                        myToolWindowInstance.appendLog("[DEBUG] VirtualFile path: ${msg.virtualFile.path}")
                        // Determine method based on error line
                        val vFile = msg.virtualFile
                        val document = FileDocumentManager.getInstance().getDocument(vFile)
                        // Try to use msg.lineNumber, fallback to msg.line if necessary
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
                        myToolWindowInstance.appendLog("[DEBUG] Determined lineNum: $lineNum")
                        val offset = document?.getLineStartOffset(lineNum - 1) ?: 0
                        myToolWindowInstance.appendLog("[DEBUG] Calculated offset for error: $offset")
                        val psiFile = PsiManager.getInstance(project).findFile(vFile)
                        val psiElement = psiFile?.findElementAt(offset)
                        val psiMethod = PsiTreeUtil.getParentOfType(
                            psiElement, PsiMethod::class.java, false
                        )
                        myToolWindowInstance.appendLog("[DEBUG] Resolved psiMethod: ${psiMethod?.name}")
                        val methodName = psiMethod?.name
                            ?: vFile.name.substringBeforeLast(".java")
                        val errorMap = mapOf(
                            "method_name" to methodName,
                            "error_message" to msg.message,
                            "stack_trace" to msg.message
                        )
                        myToolWindowInstance.appendLog("[DEBUG] Adding errorMap: method_name=$methodName")
                        errosDeTesteGlobal.add(errorMap)
                    }
                    processRetry(project)
                } else {
                    myToolWindowInstance.appendLog("[DEBUG] Compilação bem-sucedida para $packageName.$className. Executando JUnit Runner.")
                    reexecuteJUnitRunner(project, latestGeneratedTestClassFqn)
                }
            }
        }
    }

    private fun reexecuteJUnitRunner(project: Project, generatedTestClassFqn: String) {
        myToolWindowInstance.appendLog("[DEBUG] reexecuteJUnitRunner called with generatedTestClassFqn='$generatedTestClassFqn'")
        // Parse fully-qualified name into package and class
        val lastDot = generatedTestClassFqn.lastIndexOf('.')
        val packageName = if (lastDot != -1) generatedTestClassFqn.substring(0, lastDot) else ""
        val className = if (lastDot != -1) generatedTestClassFqn.substring(lastDot + 1) else generatedTestClassFqn
        myToolWindowInstance.appendLog("[DEBUG] Parsed packageName='$packageName', className='$className'")
        myToolWindowInstance.appendLog("[DEBUG] Scheduling execution on pooled thread for $packageName.$className")
        ApplicationManager.getApplication().executeOnPooledThread {
            myToolWindowInstance.appendLog("[DEBUG] In pooled thread, resolving PsiClass for '$packageName.$className'")
            val psiClass = ApplicationManager.getApplication().runReadAction<com.intellij.psi.PsiClass?> {
                com.intellij.psi.JavaPsiFacade.getInstance(project)
                    .findClass(
                        if (packageName.isNotBlank()) "$packageName.$className" else className,
                        com.intellij.psi.search.GlobalSearchScope.projectScope(project)
                    )
            }

            if (psiClass != null) {
                ApplicationManager.getApplication().invokeLater {
                    myToolWindowInstance.appendLog("[DEBUG] PsiClass resolved: '${psiClass.name}'. Preparing JUnit run configuration.")
                    val runManager = com.intellij.execution.RunManager.getInstance(project)
                    val configurationFactory = com.intellij.execution.junit.JUnitConfigurationType
                        .getInstance().configurationFactories[0]
                    val settings = runManager.createConfiguration("$className [UnitCat]", configurationFactory)
                    val configuration = settings.configuration as com.intellij.execution.junit.JUnitConfiguration

                    configuration.setMainClass(psiClass)
                    configuration.setModule(ModuleManager.getInstance(project).modules.firstOrNull())

                    runManager.addConfiguration(settings)
                    runManager.selectedConfiguration = settings

                    executeConfiguration(
                        settings,
                        com.intellij.execution.executors.DefaultRunExecutor.getRunExecutorInstance()
                    )
                    myToolWindowInstance.appendLog("[INFO] Classe de teste executada diretamente via JUnit runner.")
                }
            } else {
                myToolWindowInstance.appendLog("[DEBUG][ERROR] PsiClass not found for '$packageName.$className'")
            }
        }
    }

    companion object {
        @JvmStatic
        val errosDeTesteGlobal: MutableList<Map<String, String>> = mutableListOf()
    }
}

