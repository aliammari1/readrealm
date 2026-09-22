import Foundation

struct ReadingStatistics {
    var timeSpentReading: TimeInterval = 0
    var pagesRead: Int = 0
    var averageReadingSpeed: Double = 0
    var lastReadPage: Int = 0
    
    mutating func updateStats(currentPage: Int, wordsRead: Int, timeElapsed: TimeInterval) {
        pagesRead = max(pagesRead, currentPage + 1)
        timeSpentReading += timeElapsed
        averageReadingSpeed = Double(wordsRead) / (timeSpentReading / 60)
        lastReadPage = currentPage
    }
}
