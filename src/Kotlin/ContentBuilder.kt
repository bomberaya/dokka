package org.jetbrains.dokka

import org.jetbrains.markdown.MarkdownElementTypes
import java.util.ArrayDeque
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor
import org.jetbrains.jet.lang.resolve.name.Name
import org.jetbrains.jet.lang.resolve.DescriptorToSourceUtils

public fun DocumentationBuilder.buildContent(tree: MarkdownTree, descriptor: DeclarationDescriptor): Content {
    val nodeStack = ArrayDeque<ContentNode>()
    nodeStack.push(Content())

    tree.visit {(node, text, processChildren) ->
        val parent = nodeStack.peek()!!
        val nodeType = node.getTokenType()
        val nodeText = tree.getNodeText(node)
        when (nodeType) {
            MarkdownElementTypes.BULLET_LIST -> {
                nodeStack.push(ContentList())
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.ORDERED_LIST -> {
                nodeStack.push(ContentList()) // TODO: add list kind
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.HORIZONTAL_RULE -> {
            }
            MarkdownElementTypes.LIST_BLOCK -> {
                nodeStack.push(ContentBlock())
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.EMPH -> {
                nodeStack.push(ContentEmphasis())
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.STRONG -> {
                nodeStack.push(ContentStrong())
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.CODE -> {
                nodeStack.push(ContentCode())
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.ANONYMOUS_SECTION -> {
                nodeStack.push(ContentSection(""))
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.DIRECTIVE -> {
                val name = tree.findChildByType(node, MarkdownElementTypes.DIRECTIVE_NAME)?.let { tree.getNodeText(it) } ?: ""
                val params = tree.findChildByType(node, MarkdownElementTypes.DIRECTIVE_PARAMS)?.let { tree.getNodeText(it) } ?: ""
                when (name) {
                    "code" -> parent.append(functionBody(descriptor, params))
                }
            }
            MarkdownElementTypes.NAMED_SECTION -> {
                val label = tree.findChildByType(node, MarkdownElementTypes.SECTION_NAME)?.let { tree.getNodeText(it) } ?: ""
                nodeStack.push(ContentSection(label))
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.LINK -> {
                val target = tree.findChildByType(node, MarkdownElementTypes.TARGET)?.let { tree.getNodeText(it) } ?: ""
                val href = tree.findChildByType(node, MarkdownElementTypes.HREF)?.let { tree.getNodeText(it) }
                val link = if (href != null) ContentExternalLink(href) else ContentExternalLink(target)
                link.append(ContentText(target))
                parent.append(link)
            }
            MarkdownElementTypes.PLAIN_TEXT -> {
                nodeStack.push(ContentText(nodeText))
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.END_LINE -> {
                nodeStack.push(ContentText(nodeText))
                processChildren()
                parent.append(nodeStack.pop())
            }
            MarkdownElementTypes.BLANK_LINE -> {
                processChildren()
            }
            MarkdownElementTypes.PARA -> {
                nodeStack.push(ContentParagraph())
                processChildren()
                parent.append(nodeStack.pop())
            }
            else -> {
                processChildren()
            }
        }
    }
    return nodeStack.pop() as Content
}


fun DocumentationBuilder.functionBody(descriptor: DeclarationDescriptor, functionName: String): ContentNode {
    var scope = context.getResolutionScope(descriptor)
    val parts = functionName.split('.')
    var symbol: DeclarationDescriptor? = null

    for (part in parts) {
        // short name
        val symbolName = Name.guess(part)
        val partSymbol = scope.getLocalVariable(symbolName) ?:
                scope.getProperties(symbolName).firstOrNull() ?:
                scope.getFunctions(symbolName).firstOrNull() ?:
                scope.getClassifier(symbolName) ?:
                scope.getPackage(symbolName)

        if (partSymbol == null)
            break
        scope = context.getResolutionScope(partSymbol)
        symbol = partSymbol
    }

    if (symbol != null) {
        val psi = DescriptorToSourceUtils.descriptorToDeclaration(descriptor)


    }
    return ContentCode().let() { it.append(ContentText("inline")); it }
}