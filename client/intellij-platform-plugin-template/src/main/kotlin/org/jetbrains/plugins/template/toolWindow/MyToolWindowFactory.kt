package org.jetbrains.plugins.template.toolWindow

import com.intellij.execution.ProgramRunnerUtil.executeConfiguration
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.plugins.template.services.MyProjectService
import java.io.ByteArrayOutputStream
import javax.swing.JButton
import javax.swing.JScrollPane
import javax.swing.JTextArea

private data class ParsedInitResponse(
    val scenarios: String,
    val dependenciesMap: Map<String, String>
)

data class InitResponseDTO(
    val analysisResponseDTO: AnalysisResponseDTO,
    val customDependencies: List<String>
)

data class AnalysisResponseDTO(
    val classFqn: String,
    val purposeSummary: String,
    val mainMethodSignature: String,
    val inputType: String,
    val outputType: String
)

data class CompleteResponseDTO(
    val generatedTestClassFqn: String,
    val generatedTestCode: String
)

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
        // Registrar listener de teste após criar o conteúdo
        registrarListenerDeTeste(project)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        private val textArea = JTextArea(10, 30).apply {
            lineWrap = true
            wrapStyleWord = true
            border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        // Propriedade para armazenar o arquivo de teste gerado
        private lateinit var testFile: java.io.File

        // Propriedade para armazenar a estrutura dos métodos de teste: nome -> código
        private val estruturaDeTestes: MutableMap<String, String> = mutableMapOf()

        // Variáveis elevadas para uso em processTerminated
        private var targetClassName: String = ""
        private var targetClassPackage: String = ""
        private var targetClassCode: String = ""
        private var guidelines: String = ""
        private var dependenciasCodigo: String = ""
        private var parsedResponse: ParsedInitResponse? = null

        // Propriedade para armazenar os erros de teste capturados
        // Agora é uma propriedade de classe, privada, acessível em toda a instância
        private val errosDeTeste: MutableList<Map<String, String>> = mutableListOf()

        fun getContent() = buildUI()

        private fun buildUI() = JBPanel<JBPanel<*>>(java.awt.BorderLayout()).apply {
            val label = JBLabel("Diretrizes de criação de testes do seu projeto:").apply {
                border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }
            add(label, java.awt.BorderLayout.NORTH)
            add(createCenterPanel(), java.awt.BorderLayout.CENTER)
        }

        private fun createGenerateTestsButton(): JButton {
            return JButton("Gerar testes").apply {
                maximumSize = java.awt.Dimension(150, 30)
                addActionListener { onGerarTestesClicked() }
            }
        }

        private fun createButtonPanel(): JBPanel<JBPanel<*>> {
            return JBPanel<JBPanel<*>>(java.awt.BorderLayout()).apply {
                maximumSize = java.awt.Dimension(Int.MAX_VALUE, 40)
                add(javax.swing.JComboBox(arrayOf("Em Lote", "Por Cenário")).apply {
                    maximumSize = java.awt.Dimension(150, 30)
                }, java.awt.BorderLayout.WEST)
                add(createGenerateTestsButton(), java.awt.BorderLayout.EAST)
            }
        }

        private fun createCenterPanel(): JBPanel<JBPanel<*>> {
            val scrollPane = JScrollPane(textArea).apply {
                preferredSize = java.awt.Dimension(Short.MAX_VALUE.toInt(), 200)
                maximumSize = java.awt.Dimension(Int.MAX_VALUE, 200)
            }
            return JBPanel<JBPanel<*>>().apply {
                layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
                add(scrollPane)
                add(javax.swing.Box.createVerticalStrut(10))
                add(createButtonPanel())
            }
        }

        private fun onGerarTestesClicked() {
            val project = toolWindow.project
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                val document = editor.document
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                if (psiFile is PsiJavaFile) {
                    // Verificação para evitar autoanálise de arquivos de teste
                    if (psiFile.name.contains("Test", ignoreCase = true)) {
                        println("⚠️ [UnitCat] Arquivo com nome 'Test' detectado. Operação cancelada para evitar autoanálise.")
                        return
                    }
                    targetClassName = psiFile.name
                    targetClassPackage = psiFile.packageName
                    targetClassCode = document.text

                    // Envia requisição /init e processa resposta
                    enviarRequisicaoInit(project)
                } else {
                    println("O arquivo ativo não é um arquivo Java.")
                }
            } else {
                println("Nenhum editor ativo encontrado.")
            }
        }

        private fun buscarCodigoDasDependencias(project: Project, dependenciesMap: Map<String, String>): String {
            val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)
            val scope = com.intellij.psi.search.GlobalSearchScope.allScope(project)

            return dependenciesMap.values.joinToString("\n\n") { qualifiedName ->
                val psiClass = facade.findClass(qualifiedName, scope)
                psiClass?.containingFile?.text
                    ?: "// Classe não encontrada no projeto: $qualifiedName"
            }
        }

        private fun enviarRequisicaoInit(project: Project) {
            println("🟡 [UnitCat] Classe alvo: $targetClassName (pacote: $targetClassPackage)")
            println("🟡 [UnitCat] Enviando requisição /init...")

            // Envia requisição apenas para obter dependências do PSI, sem depender do parsing do JSON
            // Os dados principais já estão em targetClassName, targetClassPackage, targetClassCode
            // As dependências são obtidas do PSI, se possível
            val dependenciesMap: Map<String, String> = obterDependenciasDoPsi(project, targetClassPackage, targetClassName)
            dependenciasCodigo = buscarCodigoDasDependencias(project, dependenciesMap)
            println("✅ [UnitCat] Códigos das dependências localizados.")
            println("📄 [UnitCat] Conteúdo das dependências localizadas:\n$dependenciasCodigo")

            guidelines = textArea.text

            // Armazena dependências para uso posterior, se necessário
            parsedResponse = ParsedInitResponse(
                scenarios = "",
                dependenciesMap = dependenciesMap
            )

            enviarRequisicaoComplete(project)
        }

        // Novo método para obter dependências do PSI (stub: pode ser aprimorado)
        private fun obterDependenciasDoPsi(project: Project, pkg: String, className: String): Map<String, String> {
            // Aqui, você pode implementar lógica para analisar imports do arquivo PSI e obter os FQNs das dependências customizadas
            // Por simplicidade, retorna vazio neste exemplo
            val facade = com.intellij.psi.JavaPsiFacade.getInstance(project)
            val scope = com.intellij.psi.search.GlobalSearchScope.allScope(project)
            val psiClass = facade.findClass(if (pkg.isNotBlank()) "$pkg.${className.removeSuffix(".java")}" else className.removeSuffix(".java"), scope)
            if (psiClass != null && psiClass is com.intellij.psi.PsiClassOwner) {
                val importList = (psiClass.containingFile as? PsiJavaFile)?.importList
                val customDeps = importList?.allImportStatements?.mapNotNull { it.importReference?.qualifiedName }?.filter {
                    // Exclui dependências padrão do Java/JUnit
                    !it.startsWith("org.junit") && !it.startsWith("java.") && !it.startsWith("javax.")
                } ?: emptyList()
                return customDeps.associateWith { it }
            }
            return emptyMap()
        }

        private fun enviarRequisicaoComplete(project: Project) {
            // Logs detalhados antes de enviar a requisição
            println("🟡 [UnitCat] Enviando requisição /complete...")
            println("🟡 [UnitCat] Payload enviado:")
            println("targetClassName = $targetClassName")
            println("targetClassCode (truncado) = ${targetClassCode.take(100)}...")
            println("targetClassPackage = $targetClassPackage")
            println("guidelines = ${guidelines.take(100)}...")
            println("dependenciesName = ${(parsedResponse?.dependenciesMap?.values?.joinToString(",") ?: "")}")

            val completeRequestBody = listOf(
                "targetClassName" to targetClassName,
                "targetClassCode" to targetClassCode,
                "targetClassPackage" to (targetClassPackage ?: ""),
                "guidelines" to guidelines,
                "dependencies" to dependenciasCodigo,
                "dependenciesName" to (parsedResponse?.dependenciesMap?.values?.joinToString(",") ?: "")
            ).joinToString("&") { (k, v) ->
                "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
            }
            val client = java.net.http.HttpClient.newHttpClient()
            val completeRequest = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:8080/unitcat/api/complete"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(completeRequestBody))
                .build()
            try {
                val completeResponse = client.send(completeRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
                val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                val completeResponseDTO = objectMapper.readValue(completeResponse.body(), CompleteResponseDTO::class.java)
                // Logs detalhados após receber a resposta
                println("✅ [UnitCat] Resposta /complete recebida.")
                println("📦 [UnitCat] Classe gerada: ${completeResponseDTO.generatedTestClassFqn}")
                println("📄 [UnitCat] Código gerado (preview):\n${completeResponseDTO.generatedTestCode.lines().take(10).joinToString("\n")}")

                criarClasseDeTeste(project, completeResponseDTO.generatedTestCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun criarClasseDeTeste(project: Project, testClassContentRaw: String) {
            // Log visual no início do processo
            println("⚙️ [UnitCat] Iniciando criação da classe de teste...")
            // ====== Cria a classe de teste espelhando o pacote em /src/test/java ======
            val testClassContent = testClassContentRaw.removePrefix("```java").removeSuffix("```").trim()
            // Extrai o pacote da resposta
            val packageLine = testClassContent.lines().firstOrNull { it.trim().startsWith("package ") }
            val packageName = packageLine?.removePrefix("package")?.removeSuffix(";")?.trim() ?: ""
            println("📦 [UnitCat] Pacote identificado: '$packageName'")
            // Constrói o caminho de diretório com base no package
            val packagePath = packageName.replace('.', '/')
            val testRoot = java.io.File(project.basePath, "src/test/java/$packagePath")
            println("📁 [UnitCat] Diretório alvo: ${testRoot.absolutePath}")
            if (!testRoot.exists()) {
                testRoot.mkdirs()
            }
            // Extrai o nome da classe da primeira ocorrência de "class X"
            val classNameRegex = Regex("""class\s+(\w+)""")
            val match = classNameRegex.find(testClassContent)
            val className = match?.groups?.get(1)?.value ?: "GeneratedTest"
            testFile = java.io.File(testRoot, "$className.java")
            // Log antes de salvar o arquivo
            println("💾 [UnitCat] Salvando classe de teste em: ${testFile.absolutePath}")
            // Escreve o conteúdo no arquivo
            testFile.writeText(testClassContent)
            println("✅ [UnitCat] Arquivo salvo com sucesso.")

            // --- NOVO PARSER: dividir header, testMethods (@Test), footer ---
            val lines = testClassContent.lines()
            val headerLines = mutableListOf<String>()
            val footerLines = mutableListOf<String>()
            val currentMethod = StringBuilder()
            var insideTestMethod = false
            var currentMethodName = ""
            val testMethods = mutableMapOf<String, String>()
            var braceBalance = 0
            var foundFirstTest = false
            val testAnnotationRegex = Regex("""^\s*@Test""")
            val methodHeaderRegex = Regex("""\s*void\s+(\w+)\s*\(\)\s*\{""")

            var i = 0
            while (i < lines.size) {
                val line = lines[i]
                if (!foundFirstTest && testAnnotationRegex.containsMatchIn(line)) {
                    foundFirstTest = true
                }

                if (!foundFirstTest) {
                    headerLines.add(line)
                    i++
                    continue
                }

                if (testAnnotationRegex.containsMatchIn(line)) {
                    insideTestMethod = true
                    currentMethod.clear()
                    // Captura anotações imediatamente acima
                    var annotationStartIdx = i
                    while (annotationStartIdx > 0 && lines[annotationStartIdx - 1].trim().startsWith("@")) {
                        annotationStartIdx--
                    }
                    for (j in annotationStartIdx..i) {
                        currentMethod.appendLine(lines[j])
                    }
                    braceBalance = 0
                    // Agora, avançar para capturar o cabeçalho do método e corpo
                    i++
                    // Captura linhas até encontrar o método (void ... {)
                    var methodHeaderFound = false
                    while (i < lines.size && !methodHeaderFound) {
                        val l = lines[i]
                        currentMethod.appendLine(l)
                        if (l.contains("{")) {
                            braceBalance += l.count { it == '{' }
                            braceBalance -= l.count { it == '}' }
                            methodHeaderFound = true
                        }
                        i++
                    }
                    // Agora, capturar corpo do método até fechar todos os braces
                    while (i < lines.size && braceBalance > 0) {
                        val l = lines[i]
                        currentMethod.appendLine(l)
                        if ("{" in l) braceBalance += l.count { it == '{' }
                        if ("}" in l) braceBalance -= l.count { it == '}' }
                        i++
                    }
                    // Após sair do loop, já temos o método completo
                    val methodText = currentMethod.toString()
                    val nameMatch = methodHeaderRegex.find(methodText)
                    val name = nameMatch?.groups?.get(1)?.value ?: "unknown"
                    testMethods[name] = methodText.trim()
                    insideTestMethod = false
                    continue
                }

                if (!insideTestMethod) {
                    footerLines.add(line)
                }
                i++
            }

            val header = headerLines.joinToString("\n")
            val footer = footerLines.joinToString("\n")

            // Salva estrutura
            estruturaDeTestes.clear()
            estruturaDeTestes.putAll(testMethods)

            // 🧩 [UnitCat] Estrutura de testes populada com os seguintes métodos:
            println("🧩 [UnitCat] Estrutura de testes populada com os seguintes métodos:")
            estruturaDeTestes.forEach { (nome, codigo) ->
                println("  🔹 $nome -> ${codigo.lines().firstOrNull()?.take(100)}...")
            }

            // --- Reconstruir classe usando header, estruturaDeTestes, footer ---
            val rebuiltClass = buildString {
                appendLine(header.trim())
                appendLine()
                // Para cada método, adiciona o corpo completo exatamente como gerado
                estruturaDeTestes.values.forEach { methodCode ->
                    appendLine(methodCode)
                    appendLine()
                }
                appendLine(footer.trim())
            }
            testFile.writeText(rebuiltClass)

            // 📄 [UnitCat] Classe de teste reconstruída com base na estrutura interna. Conteúdo inicial:
            println("📄 [UnitCat] Classe de teste reconstruída com base na estrutura interna. Conteúdo inicial:")
            println(rebuiltClass.lines().take(20).joinToString("\n"))

            // Abre a classe criada automaticamente no editor
            val newVirtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                .refreshAndFindFileByIoFile(testFile)
            if (newVirtualFile != null) {
                println("📂 [UnitCat] Tentando abrir a classe de teste no editor: ${testFile.name}")
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(newVirtualFile, true)
                println("✅ [UnitCat] Classe de teste aberta com sucesso.")
            } else {
                println("❌ [UnitCat] Falha ao localizar o arquivo virtual para: ${testFile.absolutePath}")
            }
            // Executa a classe de teste diretamente pela API da IDE
            val psiClass = com.intellij.psi.JavaPsiFacade.getInstance(project)
                .findClass("$packageName.$className", com.intellij.psi.search.GlobalSearchScope.projectScope(project))

            if (psiClass != null) {
                val runManager = com.intellij.execution.RunManager.getInstance(project)
                val configurationFactory = com.intellij.execution.junit.JUnitConfigurationType.getInstance().configurationFactories[0]
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
                println("🚀 [UnitCat] Classe de teste executada diretamente via JUnit runner.")
            } else {
                println("❌ [UnitCat] Não foi possível localizar a classe de teste '$packageName.$className' para execução.")
            }
            // Após criar o arquivo de teste, montar o comando para executar os testes
            println("[UnitCat] Executando testes Maven...")
            executarGoalMaven(project, 0)
        }




        private fun executarGoalMaven(project: Project, retryCount: Int = 0, goal: String = "test") {
            val mavenProject = org.jetbrains.idea.maven.project.MavenProjectsManager.getInstance(project).projects.firstOrNull() ?: return

            val projectDirPath = mavenProject.directory

            val parameters = MavenRunnerParameters(
                true,
                projectDirPath,
                null as String?,
                listOf(goal),
                emptyList()
            )

            val settings = MavenRunnerSettings()
            val generalSettings: MavenGeneralSettings? = null

            val outputStream = ByteArrayOutputStream()
            val processListener = object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    super.onTextAvailable(event, outputType)
                    outputStream.write(event.text.toByteArray())
                }
                override fun startNotified(event: ProcessEvent) {
                    super.startNotified(event)
                }
                override fun processTerminated(event: ProcessEvent) {
                    super.processTerminated(event)
                    // LOG 1: Antes da verificação de retryCount
                    println("🔁 [UnitCat] Iniciando execução do Maven com retryCount = $retryCount")
                    if (retryCount >= 3) {
                        println("🔁 [UnitCat] Limite de 3 tentativas de retry atingido.")
                        return
                    }
                    val logContent = outputStream.toString(Charsets.UTF_8.name())
                    println("📜 [UnitCat] Logs Maven capturados.")

                    // Coleta erros de teste da execução local (não global)
                    val errosDeTesteFinal = errosDeTeste.toList()
                    // Limpa a lista de erros para próxima execução
                    errosDeTeste.clear()

                    // Serializa a lista de erros como JSON
                    val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                    println("📋 [UnitCat] Estrutura de dados completa dos testes com falha:")
                    println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errosDeTesteFinal))

                    println("🔁 [UnitCat] Enviando requisição /retry com ${errosDeTesteFinal.size} testes falhos...")
                    // LOG 2: Antes de construir corpo da requisição retry
                    println("🔁 [UnitCat] Construindo corpo da requisição /retry...")
                    // Recupera novamente o conteúdo da classe de teste do PSI, se possível, ou do arquivo salvo
                    val testClassCodeAtual: String = try {
                        val vFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(testFile)
                        if (vFile != null) {
                            val doc = FileDocumentManager.getInstance().getDocument(vFile)
                            doc?.text ?: testFile.readText()
                        } else {
                            testFile.readText()
                        }
                    } catch (e: Exception) {
                        testFile.readText()
                    }

                    val formParams = mutableListOf<Pair<String, String>>(
                        "targetClassName" to targetClassName,
                        "targetClassPackage" to targetClassPackage,
                        "targetClassCode" to targetClassCode,
                        "testClassCode" to testClassCodeAtual,
                        "dependencies" to dependenciasCodigo,
                        "dependenciesName" to (parsedResponse?.dependenciesMap?.values?.joinToString(",") ?: "")
                    )
                    // Serializa erros como JSON e adiciona ao formParams
                    val failingTestsJson = objectMapper.writeValueAsString(errosDeTesteFinal)
                    formParams.add("failingTestDetailsRequestDTOS" to failingTestsJson)

                    val retryRequestBody = formParams.joinToString("&") { (key, value) ->
                        "${java.net.URLEncoder.encode(key, "UTF-8")}=${java.net.URLEncoder.encode(value, "UTF-8")}"
                    }

                    println("✅ [UnitCat] Processo Maven finalizado (código: ${event.exitCode})")

                    val errorLines = logContent.lines()
                        .filter { it.contains("[ERROR]") || it.contains("COMPILATION ERROR") || it.contains("BUILD FAILURE") }
                        .joinToString("\n")

                    // NOVO TRECHO: Mostra mensagem apropriada, mas SEM retornar, sempre chama o endpoint /retry
                    if (errorLines.isNotEmpty()) {
                        println("❌ [UnitCat] Foram encontrados erros '[ERROR]' no log Maven.")
                        // LOG 3: Antes de criar o retryRequest
                        println("🔁 [UnitCat] Enviando requisição HTTP para /retry...")
                        // Chamada HTTP para o endpoint /retry com application/x-www-form-urlencoded
                        val retryRequest = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8080/unitcat/api/retry"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(retryRequestBody))
                            .build()
                        try {
                            val retryResponse = java.net.http.HttpClient.newHttpClient()
                                .send(retryRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
                            println("✅ [UnitCat] Resposta /retry recebida:")
                            println("📦 [UnitCat] Conteúdo:\n${retryResponse.body()}")

                            // 🔧 [UnitCat] Atualizando métodos da classe de teste com base na resposta /retry
                            val responseJson = retryResponse.body()
                            // LOG 4: Após parsear o JSON de resposta
                            val retryResultNode = objectMapper.readTree(responseJson)
                            println("🔁 [UnitCat] Resposta JSON de /retry parseada com sucesso.")
                            // ===== INÍCIO: Atualiza métodos da classe de teste diretamente com base na resposta /retry =====
                            val modifiedMethods = retryResultNode.get("modifiedTestMethods")
                            val modifiedMethodsList = mutableListOf<Pair<String, String>>()
                            modifiedMethods?.forEach { methodNode ->
                                val methodName = methodNode.get("methodName").asText()
                                val modifiedCode = methodNode.get("modifiedCode").asText().trim()
                                modifiedMethodsList.add(methodName to modifiedCode)
                                println("🔁 [UnitCat] Método '${methodName}' recebido para substituição.")
                            }
                            substituirMetodosNoArquivo(testFile, modifiedMethodsList)
                            println("📄 [UnitCat] Classe de teste atualizada com métodos do /retry.")
                            val requiredImports = retryResultNode.get("requiredNewImports")
                            println("🔁 [UnitCat] Quantidade de métodos modificados recebidos: ${modifiedMethods?.size() ?: 0}")
                            println("🔁 [UnitCat] Quantidade de novos imports recebidos: ${requiredImports?.size() ?: 0}")
                            ApplicationManager.getApplication().invokeLater {
                                com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
                                    val document = FileDocumentManager.getInstance().getDocument(
                                        com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(testFile)!!
                                    )!!
                                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                                    // (manipulação de imports pode ser feita aqui futuramente)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            println("❌ [UnitCat] Falha ao chamar o endpoint /retry: ${e.message}")
                        }
                    } else {
                        println("✅ [UnitCat] Nenhum erro '[ERROR]' identificado no log Maven.")
                    }
                }
            }

            ApplicationManager.getApplication().invokeAndWait { FileDocumentManager.getInstance().saveAllDocuments() }

            MavenRunConfigurationType.runConfiguration(
                project,
                parameters,
                generalSettings,
                settings,
                object : ProgramRunner.Callback {
                    override fun processStarted(descriptor: RunContentDescriptor) {
                        val handler = descriptor.processHandler
                        if (handler != null) {
                            handler.addProcessListener(processListener)
                        } else {
                            println("❗ [UnitCat] Erro: ProcessHandler é nulo após o início do processo.")
                        }
                    }
                },
                false
            )
        }
    }

    private fun registrarListenerDeTeste(project: Project) {
        val connection: MessageBusConnection = project.messageBus.connect(project as Disposable)
        // Para acessar a propriedade errosDeTeste da classe MyToolWindow, precisamos de uma instância.
        // Buscamos a instância do tool window.
        val myToolWindowInstance = toolWindowInstanceFromProject(project)
        connection.subscribe(SMTRunnerEventsListener.TEST_STATUS, object : SMTRunnerEventsListener {
            override fun onTestingStarted(p0: SMTestProxy.SMRootTestProxy) {}
            override fun onTestingFinished(p0: SMTestProxy.SMRootTestProxy) {}
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
            override fun onTestFinished(testProxy: SMTestProxy) {
                // Removido o log de testes bem-sucedidos
            }

            override fun onTestFailed(testProxy: SMTestProxy) {
                // PSI-based logic to locate the test method and its code
                val project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull() ?: return
                // Para garantir thread safety, toda leitura de PSI deve ocorrer dentro de runReadAction
                ApplicationManager.getApplication().runReadAction {
                    val location = testProxy.getLocation(project, com.intellij.psi.search.GlobalSearchScope.projectScope(project))
                    val psiElement = location?.psiElement
                    val psiMethod = com.intellij.psi.util.PsiTreeUtil.getContextOfType(psiElement, com.intellij.psi.PsiMethod::class.java, false)
                    val nomeMetodoReal = psiMethod?.name ?: testProxy.name
                    val codigoMetodo = psiMethod?.text ?: "Código não encontrado"
                    val displayName = testProxy.name
                    val erro = mapOf(
                        "nomeMetodo" to nomeMetodoReal,
                        "displayName" to displayName,
                        "codigoMetodo" to codigoMetodo,
                        "mensagemErro" to (testProxy.errorMessage ?: "sem mensagem"),
                        "stacktrace" to (testProxy.stacktrace ?: "sem stacktrace")
                    )
                    // Adiciona à lista de erros da instância do MyToolWindow, se disponível
                    // Como não conseguimos acessar a instância diretamente, podemos usar um singleton ou referência global.
                    // Para garantir que a lista global de erros seja usada, vamos armazená-la de forma estática.
                    MyToolWindowFactory.errosDeTesteGlobal.add(erro)
                }
                // Removido o bloco de exibição da lista de erros aqui para evitar repetição
            }
        })
    }

    // Função utilitária para obter a instância de MyToolWindow associada ao projeto (singleton por ToolWindow)
    private fun toolWindowInstanceFromProject(project: Project): MyToolWindowFactory.MyToolWindow? {
        // Não é possível acessar diretamente a instância MyToolWindow, então retornamos null.
        return null
    }
    companion object {
        // Lista global de erros de teste para garantir acesso entre listeners e instância de janela
        @JvmStatic
        val errosDeTesteGlobal: MutableList<Map<String, String>> = mutableListOf()
    }
}
/**
 * Substitui diretamente os métodos @Test no arquivo de teste com base no nome dos métodos.
 * @param testFile Arquivo da classe de teste.
 * @param novosMetodos Lista de pares (nome, código completo do método incluindo assinatura e corpo) dos métodos a serem substituídos.
 */
private fun substituirMetodosNoArquivo(testFile: java.io.File, novosMetodos: List<Pair<String, String>>) {
    if (novosMetodos.isEmpty()) return
    val conteudoOriginal = testFile.readText()
    val linhas = conteudoOriginal.lines()
    val testAnnotationRegex = Regex("""^\s*@Test""")
    val methodHeaderRegex = Regex("""\s*void\s+(\w+)\s*\(\)\s*\{""")
    val novosMetodosMap = novosMetodos.toMap()

    val resultado = mutableListOf<String>()
    var i = 0
    while (i < linhas.size) {
        val linha = linhas[i]
        if (testAnnotationRegex.containsMatchIn(linha)) {
            // Encontrou início de método @Test
            val anotacoes = mutableListOf<String>()
            var annotationStartIdx = i
            while (annotationStartIdx > 0 && linhas[annotationStartIdx - 1].trim().startsWith("@")) {
                annotationStartIdx--
            }
            for (j in annotationStartIdx..i) {
                anotacoes.add(linhas[j])
            }

            // Avança para o cabeçalho do método
            var j = i + 1
            val metodoBuilder = StringBuilder()
            anotacoes.forEach { metodoBuilder.appendLine(it) }
            var braceBalance = 0
            var headerFound = false
            while (j < linhas.size && !headerFound) {
                val l = linhas[j]
                metodoBuilder.appendLine(l)
                if (l.contains("{")) {
                    braceBalance += l.count { it == '{' }
                    braceBalance -= l.count { it == '}' }
                    headerFound = true
                }
                j++
            }

            // Captura corpo do método
            while (j < linhas.size && braceBalance > 0) {
                val l = linhas[j]
                metodoBuilder.appendLine(l)
                if ("{" in l) braceBalance += l.count { it == '{' }
                if ("}" in l) braceBalance -= l.count { it == '}' }
                j++
            }

            val metodoOriginal = metodoBuilder.toString()
            val nomeMetodo = methodHeaderRegex.find(metodoOriginal)?.groups?.get(1)?.value
            if (nomeMetodo != null && novosMetodosMap.containsKey(nomeMetodo)) {
                val novoMetodoCompleto = novosMetodosMap[nomeMetodo]!!
                resultado.addAll(novoMetodoCompleto.lines())
                resultado.add("") // separador visual
                println("🔁 [UnitCat] Método '$nomeMetodo' substituído diretamente no arquivo.")
            } else {
                resultado.addAll(metodoOriginal.lines())
                resultado.add("")
            }
            i = j
            continue
        }

        // Linha normal fora de @Test
        resultado.add(linha)
        i++
    }

    testFile.writeText(resultado.joinToString("\n"))
}