import SwiftUI
import PDFKit

actor PDFExporter {
    static func createPDF(
        from content: String,
        title: String,
        sourceLanguage: String,
        targetLanguage: String,
        isTranslated: Bool,
        preferences: ReadingPreferences,
        progress: @escaping (Float) -> Void
    ) async throws -> URL {
        let pdfDocument = PDFDocument()
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).pdf")
        
        // Add metadata page
        if isTranslated {
            let metadataPage = createMetadataPage(
                title: title,
                sourceLanguage: sourceLanguage,
                targetLanguage: targetLanguage,
                preferences: preferences
            )
            pdfDocument.insert(metadataPage, at: 0)
        }
        
        // Split and process content
        let words = content.split(separator: " ")
        let wordsPerPage = 300
        let pages = stride(from: 0, to: words.count, by: wordsPerPage).map { startIndex -> String in
            let endIndex = min(startIndex + wordsPerPage, words.count)
            return words[startIndex..<endIndex].joined(separator: " ")
        }
        
        let totalPages = Float(pages.count)
        print("📄 Creating PDF with \(pages.count) pages")
        
        // Process each page
        for (index, pageContent) in pages.enumerated() {
            if let pdfPage = try await createContentPage(
                content: pageContent,
                pageNumber: index + 1,
                totalPages: pages.count,
                preferences: preferences
            ) {
                pdfDocument.insert(pdfPage, at: pdfDocument.pageCount)
                await MainActor.run {
                    progress(Float(index + 1) / totalPages)
                }
            }
        }
        
        // Save PDF
        guard pdfDocument.pageCount > 0,
              pdfDocument.write(to: tempURL) else {
            throw NSError(domain: "PDFExporter", code: -1, 
                         userInfo: [NSLocalizedDescriptionKey: "Failed to write PDF"])
        }
        
        return tempURL
    }
    
    private static func createMetadataPage(
        title: String,
        sourceLanguage: String,
        targetLanguage: String,
        preferences: ReadingPreferences
    ) -> PDFPage {
        let pageRect = CGRect(x: 0, y: 0, width: 612, height: 792)
        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
        
        let data = renderer.pdfData { context in
            context.beginPage()
            let contentRect = pageRect.insetBy(dx: CGFloat(preferences.marginSize), dy: CGFloat(preferences.marginSize))
            
            let titleAttributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: preferences.fontSize * 1.5),
                .foregroundColor: UIColor(preferences.textColor)
            ]
            
            let infoAttributes: [NSAttributedString.Key: Any] = [
                .font: UIFont(name: preferences.fontFamily, size: preferences.fontSize) ?? .systemFont(ofSize: preferences.fontSize),
                .foregroundColor: UIColor(preferences.textColor),
                .paragraphStyle: {
                    let style = NSMutableParagraphStyle()
                    style.lineSpacing = preferences.lineHeight
                    style.paragraphSpacing = preferences.paragraphSpacing
                    return style
                }()
            ]
            
            // Draw metadata
            let metadata = """
                \(title)
                
                Translation Information:
                - Source Language: \(Locale.current.localizedString(forIdentifier: sourceLanguage) ?? sourceLanguage)
                - Target Language: \(Locale.current.localizedString(forIdentifier: targetLanguage) ?? targetLanguage)
                - Export Date: \(Date().formatted())
                
                Reading Settings:
                - Font: \(preferences.fontFamily)
                - Size: \(Int(preferences.fontSize))pt
                - Layout: \(preferences.layoutMode.rawValue)
                """
            
            (metadata as NSString).draw(in: contentRect, withAttributes: infoAttributes)
        }
        
        return PDFDocument(data: data)!.page(at: 0)!
    }
    
    private static func createContentPage(
        content: String,
        pageNumber: Int,
        totalPages: Int,
        preferences: ReadingPreferences
    ) async throws -> PDFPage? {
        try await withCheckedThrowingContinuation { continuation in
            autoreleasepool {
                let pageRect = CGRect(x: 0, y: 0, width: 612, height: 792) // US Letter size
                let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
                
                let data = renderer.pdfData { context in
                    context.beginPage()
                    
                    // Set up text formatting
                    let paragraphStyle = NSMutableParagraphStyle()
                    paragraphStyle.alignment = NSTextAlignment(rawValue: ["left": 0, "center": 1, "right": 2, "justify": 3][preferences.textAlignment] ?? 0) ?? .left
                    paragraphStyle.lineSpacing = preferences.lineHeight
                    paragraphStyle.paragraphSpacing = preferences.paragraphSpacing * 10
                    paragraphStyle.firstLineHeadIndent = preferences.marginSize / 2
                    
                    let textAttributes: [NSAttributedString.Key: Any] = [
                        .font: UIFont(name: preferences.fontFamily, size: preferences.fontSize) ?? .systemFont(ofSize: preferences.fontSize),
                        .paragraphStyle: paragraphStyle,
                        .foregroundColor: UIColor(preferences.textColor),
                        .kern: preferences.letterSpacing
                    ]
                    
                    // Main content area
                    let contentRect = pageRect.insetBy(dx: preferences.marginSize, dy: preferences.marginSize)
                    let contentHeight = contentRect.height - 30 // Reserve space for page number
                    
                    // Draw main content
                    (content as NSString).draw(in: CGRect(x: contentRect.minX,
                                                        y: contentRect.minY,
                                                        width: contentRect.width,
                                                        height: contentHeight),
                                            withAttributes: textAttributes)
                    
                    // Draw page number at bottom
                    let pageText = "Page \(pageNumber) of \(totalPages)"
                    let pageNumberAttributes: [NSAttributedString.Key: Any] = [
                        .font: UIFont.systemFont(ofSize: preferences.fontSize * 0.8),
                        .foregroundColor: UIColor(preferences.textColor)
                    ]
                    
                    let pageNumberSize = (pageText as NSString).size(withAttributes: pageNumberAttributes)
                    let pageNumberRect = CGRect(x: contentRect.maxX - pageNumberSize.width,
                                              y: contentRect.maxY - pageNumberSize.height,
                                              width: pageNumberSize.width,
                                              height: pageNumberSize.height)
                    
                    (pageText as NSString).draw(in: pageNumberRect, withAttributes: pageNumberAttributes)
                }
                
                if let document = PDFDocument(data: data), let page = document.page(at: 0) {
                    continuation.resume(returning: page)
                } else {
                    continuation.resume(returning: nil)
                }
            }
        }
    }
}
