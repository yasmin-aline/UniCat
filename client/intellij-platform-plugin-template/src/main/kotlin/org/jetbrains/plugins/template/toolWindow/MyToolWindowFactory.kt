package org.jetbrains.plugins.template.toolWindow

import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.template.services.MyProjectService
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
                    val targetClassName = psiFile.name
                    val targetClassPackage = psiFile.packageName
                    val targetClassCode = psiFile.fileDocument.text

                    println("🔍 Classe alvo: $targetClassName")
                    println("📦 Pacote: $targetClassPackage")
                    println("📄 Código da classe alvo:\n$targetClassCode")

                    println("📡 Enviando requisição para /unitcat/api/init...")

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
                        .uri(java.net.URI.create("http://localhost:8080/unitcat/api/init")) // Ajuste a URL conforme necessário
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                        .build()

                    // Envia a requisição e obtém a resposta (síncrono)
                    try {
                        val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                        val responseBody = response.body()
                        println("✅ Resposta da API recebida:")
                        println(responseBody)

                        val parsedResponse = parseInitResponse(responseBody)

                        println("\n🧪 Cenários identificados:")
                        println(parsedResponse.scenarios)

                        println("\n📚 Mapeamento de dependências:")
                        parsedResponse.dependenciesMap.forEach { (name, pkg) ->
                            println(" - $name -> $pkg")
                        }

                        val dependenciasCodigo = buscarCodigoDasDependencias(project, parsedResponse.dependenciesMap)
                        println("\n📄 Código-fonte das dependências localizadas:")
                        println(dependenciasCodigo)

                        val guidelines = textArea.text

                        val completeRequestBody = listOf(
                            "targetClassName" to targetClassName,
                            "targetClassCode" to targetClassCode,
                            "targetClassPackage" to (targetClassPackage ?: ""),
                            "guidelines" to guidelines,
                            "dependencies" to dependenciasCodigo,
                            "scenarios" to parsedResponse.scenarios
                        ).joinToString("&") { (k, v) ->
                            "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}"
                        }

                        val completeRequest = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("http://localhost:8080/unitcat/api/complete"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(completeRequestBody))
                            .build()

                        println("📡 Enviando requisição para /unitcat/api/complete...")
                        val completeResponse = client.send(completeRequest, java.net.http.HttpResponse.BodyHandlers.ofString())
                        println("✅ Resposta da API (/complete):\n${completeResponse.body()}")

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
                        val testFile = java.io.File(testRoot, "$className.java")

                        // Escreve o conteúdo no arquivo
                        testFile.writeText(testClassContent)
                        println("📝 Classe de teste criada em: ${testFile.absolutePath}")

                        // Abre a classe criada automaticamente no editor
                        val virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
                            .refreshAndFindFileByIoFile(testFile)

                        if (virtualFile != null) {
                            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                                .openFile(virtualFile, true)

                            val psiManager = com.intellij.psi.PsiManager.getInstance(project)
                            val psiFile = psiManager.findFile(virtualFile ?: return)
                            val psiClass = psiFile?.children?.firstOrNull { it is com.intellij.psi.PsiClass } as? com.intellij.psi.PsiClass

                            if (psiClass != null) {
                                val fullyQualifiedName = "$packageName.$className"
                                val projectDir = java.io.File(project.basePath ?: "")
                                val isMaven = java.io.File(projectDir, "pom.xml").exists()
                                val isGradle = java.io.File(projectDir, "build.gradle").exists() || java.io.File(projectDir, "build.gradle.kts").exists()

                                val command = when {
                                    isMaven -> "mvn -Dtest=$fullyQualifiedName test"
                                    isGradle -> "./gradlew test --tests $fullyQualifiedName"
                                    else -> {
                                        println("❌ Não foi possível determinar o tipo de projeto (Maven ou Gradle).")
                                        return
                                    }
                                }
                                println("▶️ Executando teste: $command")

                                try {
                                    val process = ProcessBuilder(command.split(" "))
                                        .directory(projectDir)
                                        .redirectErrorStream(true)
                                        .start()

                                    val reader = process.inputStream.bufferedReader()
                                    reader.lines().forEach { println("🧪 $it") }

                                    val exitCode = process.waitFor()
                                    println("✅ Execução finalizada com código de saída: $exitCode")
                                } catch (e: Exception) {
                                    println("❌ Erro ao executar teste: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        }

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
    }
}
