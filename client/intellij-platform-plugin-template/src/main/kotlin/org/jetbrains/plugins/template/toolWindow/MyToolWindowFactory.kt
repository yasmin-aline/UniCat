package org.jetbrains.plugins.template.toolWindow

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

        fun getContent() = JBPanel<JBPanel<*>>(java.awt.BorderLayout()).apply {
            val label = JBLabel("Diretrizes de criação de testes do seu projeto:").apply {
                border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }

            add(label, java.awt.BorderLayout.NORTH)

            val textArea = JTextArea(10, 30).apply {
                lineWrap = true
                wrapStyleWord = true
                border = javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }

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

                    println("Classe: $targetClassName")
                    println("Pacote: $targetClassPackage")
                    println("Código:\n$targetClassCode")

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
                        println("Resposta da API: $responseBody")
                        // Aqui você pode atualizar a UI ou mostrar mensagem ao usuário
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
    }
}
