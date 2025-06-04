package org.jetbrains.plugins.template.toolWindow

import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.project.MavenGeneralSettings
import org.jetbrains.plugins.template.services.MyProjectService
import java.io.ByteArrayOutputStream
import javax.swing.JButton
import javax.swing.JScrollPane
import javax.swing.JTextArea


class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
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

        // Variáveis elevadas para uso em processTerminated
        private var targetClassName: String = ""
        private var targetClassPackage: String = ""
        private var targetClassCode: String = ""
        private var guidelines: String = ""
        private var dependenciasCodigo: String = ""
        private var parsedResponse: ParsedInitResponse? = null

        fun getContent() = JBPanel<JBPanel<*>>(java.awt.BorderLayout()).apply {
            val label = JBLabel("Diretrizes de criação de testes do seu projeto:").apply {
                border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }

            add(label, java.awt.BorderLayout.NORTH)

            val scrollPane = JScrollPane(textArea).apply {
                preferredSize = java.awt.Dimension(Short.MAX_VALUE.toInt(), 200)
                maximumSize = java.awt.Dimension(Int.MAX_VALUE, 200)
            }

            val buttonPanel = JBPanel<JBPanel<*>>(java.awt.BorderLayout()).apply {
                maximumSize = java.awt.Dimension(Int.MAX_VALUE, 40)

                add(javax.swing.JComboBox(arrayOf("Em Lote", "Por Cenário")).apply {
                    maximumSize = java.awt.Dimension(150, 30)
                }, java.awt.BorderLayout.WEST)

                val gerarTestesButton = JButton("Gerar testes").apply {
                    maximumSize = java.awt.Dimension(150, 30)
                    addActionListener { onGerarTestesClicked() }
                }
                add(gerarTestesButton, java.awt.BorderLayout.EAST)
            }

            val centerPanel = JBPanel<JBPanel<*>>().apply {
                layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
                add(scrollPane)
                add(javax.swing.Box.createVerticalStrut(10))
                add(buttonPanel)
            }

            add(centerPanel, java.awt.BorderLayout.CENTER)
        }

        private fun onGerarTestesClicked() {
            val project = toolWindow.project
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                val document = editor.document
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                if (psiFile is PsiJavaFile) {
                    targetClassName = psiFile.name
                    targetClassPackage = psiFile.packageName
                    targetClassCode = document.text

                    // (Seu código anterior para enviar requisições e criar classe de teste...)
                    println("[UnitCat] Classe alvo: $targetClassName (pacote: $targetClassPackage)")
                    println("[UnitCat] Enviando requisição /init...")

                    // Monta o corpo da requisição (form-urlencoded)
                    val body = listOf(
                        "targetClassName" to targetClassName,
                        "targetClassCode" to targetClassCode,
                        "targetClassPackage" to (targetClassPackage ?: "")
                    ).joinToString("&") { (k, v) ->
                        "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
                    }

                    // Cria o cliente HTTP
                    val client = java.net.http.HttpClient.newHttpClient()

                    // Monta a requisição POST
                    val request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("http://localhost:8080/unitcat/api/init"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                        .build()

                    // Envia a requisição e obtém a resposta (síncrono)
                    try {
                        val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                        val responseBody = response.body()
                        println("[UnitCat] Resposta /init recebida com sucesso.")

                        parsedResponse = parseInitResponse(responseBody)

                        println("[UnitCat] Cenários identificados extraídos.")
                        println("[UnitCat] Dependências mapeadas: ${parsedResponse?.dependenciesMap?.size ?: 0}")
                        dependenciasCodigo = buscarCodigoDasDependencias(project, parsedResponse?.dependenciesMap ?: emptyMap())
                        println("[UnitCat] Códigos das dependências localizados.")

                        guidelines = textArea.text

                        val completeRequestBody = listOf(
                            "targetClassName" to targetClassName,
                            "targetClassCode" to targetClassCode,
                            "targetClassPackage" to (targetClassPackage ?: ""),
                            "guidelines" to guidelines,
                            "dependencies" to dependenciasCodigo,
                            "scenarios" to (parsedResponse?.scenarios ?: "")
                        ).joinToString("&") { (k, v) ->
                            "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
                        }

                        val completeRequest = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8080/unitcat/api/complete"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(completeRequestBody))
                            .build()

                        println("[UnitCat] Requisição /complete enviada e resposta recebida.")
                        val completeResponse = client.send(completeRequest, java.net.http.HttpResponse.BodyHandlers.ofString())

                        // ====== Cria a classe de teste espelhando o pacote em /src/test/java ======
                        val testClassContent = completeResponse.body().removePrefix("```java").removeSuffix("```").trim()

                        // Extrai o pacote da resposta
                        val packageLine = testClassContent.lines().firstOrNull { it.trim().startsWith("package ") }
                        val packageName = packageLine?.removePrefix("package")?.removeSuffix(";")?.trim() ?: ""

                        // Constrói o caminho de diretório com base no package
                        val packagePath = packageName.replace('.', '/')
                        val testRoot = java.io.File(project.basePath, "src/test/java/$packagePath")
                        if (!testRoot.exists()) {
                            testRoot.mkdirs()
                        }

                        // Extrai o nome da classe da primeira ocorrência de "class X"
                        val classNameRegex = Regex("""class\s+(\w+)""")
                        val match = classNameRegex.find(testClassContent)
                        val className = match?.groups?.get(1)?.value ?: "GeneratedTest"
                        testFile = java.io.File(testRoot, "$className.java")

                        // Escreve o conteúdo no arquivo
                        testFile.writeText(testClassContent)
                        println("[UnitCat] Classe de teste salva em: ${testFile.name}")

                        // Abre a classe criada automaticamente no editor
                        val virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                            .refreshAndFindFileByIoFile(testFile)

                        if (virtualFile != null) {
                            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                                .openFile(virtualFile, true)
                        }

                        // Após criar o arquivo de teste, montar o comando para executar os testes
                        println("[UnitCat] Executando testes Maven...")
                        executarGoalMaven(project, 0)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Tratar erro de rede/exceção
                    }

                } else {
                    println("O arquivo ativo não é um arquivo Java.")
                }
            } else {
                println("Nenhum editor ativo encontrado.")
            }
        }

        private data class ParsedInitResponse(
            val scenarios: String,
            val dependenciesMap: Map<String, String>
        )

        private fun parseInitResponse(response: String): ParsedInitResponse {
            val regex = Regex("Análise de Mocks por Cenário(.*?)--- FIM DA ANÁLISE DE MOCKS POR CENÁRIO ---", RegexOption.DOT_MATCHES_ALL)
            val matchResult = regex.find(response)
            val scenariosPart = matchResult?.groups?.get(1)?.value?.trim() ?: ""

            val dependenciesPart = response
                .substringAfter("Lista de Mocks", "")
                .trim()

            val dependenciesMap = dependenciesPart
                .lines()
                .mapNotNull {
                    val parts = it.split(":").map { s -> s.trim() }
                    if (parts.size == 2) parts[0] to parts[1] else null
                }
                .toMap()

            return ParsedInitResponse(scenarios = scenariosPart, dependenciesMap = dependenciesMap)
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

        private fun executarGoalMaven(project: Project, retryCount: Int = 0, goal: String = "test") {
            val mavenProject = org.jetbrains.idea.maven.project.MavenProjectsManager.getInstance(project).projects.firstOrNull() ?: return

            // Assegura que `mavenProject.directory` seja uma String de caminho.
            // O MavenRunnerParameters espera uma String.
            val projectDirPath = mavenProject.directory

            val parameters = MavenRunnerParameters(
                true,
                projectDirPath,
                null as String?,           // pomFileName (o `null as String?` é redundante aqui, `null` já funciona)
                listOf(goal),   // goals
                emptyList()     // explicitEnabledProfiles
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
                    // Verificação do limite de tentativas de retry
                    if (retryCount >= 3) {
                        println("[UnitCat] Limite de 3 tentativas de retry atingido.")
                        return
                    }
                    val logContent = outputStream.toString(Charsets.UTF_8.name())
                    println("[UnitCat] Logs Maven capturados.")
                    println("[UnitCat] Processo Maven finalizado (código: ${event.exitCode})")

                    val errorLines = logContent.lines()
                        .filter { it.contains("[ERROR]") || it.contains("COMPILATION ERROR") || it.contains("BUILD FAILURE") }
                        .joinToString("\n")

                    if (errorLines.isNotBlank()) {
                        println("[UnitCat] Erros Maven capturados (${errorLines.lines().size} linhas).")
                        val retryBody = listOf(
                            "targetClassName" to targetClassName,
                            "targetClassPackage" to targetClassPackage,
                            "targetClassCode" to targetClassCode,
                            "testClassName" to testFile.nameWithoutExtension,
                            "testClassCode" to testFile.readText(),
                            "guidelines" to guidelines,
                            "dependencies" to dependenciasCodigo,
                            "scenarios" to (parsedResponse?.scenarios ?: ""),
                            "failedTestsAndErrors" to errorLines,
                            "assertionLibrary" to "JUnit"
                        ).joinToString("&") { (k, v) -> java.net.URLEncoder.encode(k, "UTF-8") + "=" + java.net.URLEncoder.encode(v, "UTF-8") }

                        val retryRequest = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8080/unitcat/api/retry"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(retryBody))
                            .build()

                        val client = java.net.http.HttpClient.newHttpClient()
                        val retryResponse = client.send(retryRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
                        val updatedTestContent = retryResponse.body().removePrefix("```java").removeSuffix("```").trim()

                        ApplicationManager.getApplication().invokeAndWait {
                            testFile.writeText(updatedTestContent)
                            FileDocumentManager.getInstance().reloadFiles(com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByIoFile(testFile)!!)
                        }
                        println("[UnitCat] Teste reescrito com base na resposta de /retry.")
                        println("[UnitCat] Reexecutando testes Maven...")
                        executarGoalMaven(project, retryCount + 1)
                    } else {
                        println("[UnitCat] Nenhum erro '[ERROR]' identificado no log Maven.")
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
                            println("Erro: ProcessHandler é nulo após o início do processo.")
                        }
                    }
                },
                false // attachToConsole
            )
        }
    }

}