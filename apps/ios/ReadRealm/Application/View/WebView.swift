import SwiftUI
import WebKit

struct WebView: UIViewRepresentable {
    let content: String
    let preferences: ReadingPreferences
    var highlightedRange: NSRange?
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        // Apply styles and content
        let styledContent = applyStyles(to: content)
        webView.loadHTMLString(styledContent, baseURL: nil)
        
        // Apply highlighting if needed
        if let range = highlightedRange {
            let script = """
            function highlightWord(start, length) {
                const selection = window.getSelection();
                const range = document.createRange();
                const textNodes = document.evaluate('//text()', document.body, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
                
                let charCount = 0;
                for (let i = 0; i < textNodes.snapshotLength; i++) {
                    const node = textNodes.snapshotItem(i);
                    if (charCount + node.length >= start) {
                        const startOffset = start - charCount;
                        range.setStart(node, startOffset);
                        if (charCount + node.length >= start + length) {
                            range.setEnd(node, startOffset + length);
                            break;
                        }
                    }
                    charCount += node.length;
                }
                
                selection.removeAllRanges();
                selection.addRange(range);
                
                // Add highlight style
                const highlight = document.createElement('span');
                highlight.className = 'highlighted-word';
                range.surroundContents(highlight);
            }
            
            // Remove previous highlights
            document.querySelectorAll('.highlighted-word').forEach(el => {
                const parent = el.parentNode;
                while (el.firstChild) parent.insertBefore(el.firstChild, el);
                parent.removeChild(el);
            });
            
            // Apply new highlight
            highlightWord(\(range.location), \(range.length));
            """
            
            webView.evaluateJavaScript(script, completionHandler: nil)
        }
    }
    
    private func applyStyles(to content: String) -> String {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-size: \(preferences.fontSize)px;
                    line-height: \(preferences.lineHeight);
                    font-family: \(preferences.fontFamily);
                    text-align: \(preferences.textAlignment);
                    padding: \(preferences.marginSize)px;
                    background-color: \(preferences.backgroundColor.description);
                    color: \(preferences.textColor.description);
                }
                .highlighted-word {
                    background-color: \(preferences.accentColor.opacity(0.3).description);
                    border-radius: 3px;
                    transition: background-color 0.3s ease;
                }
            </style>
        </head>
        <body>
            \(content)
        </body>
        </html>
        """
    }
    
    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: WebView
        
        init(_ parent: WebView) {
            self.parent = parent
        }
    }
}
