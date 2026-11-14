package com.astahpromcp.tool.knowledge.document;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface DocumentSource {
    
    // A PDF, converted to Markdown and saved as <outputBaseName>.md.
    // When pages is not empty, only those pages are converted.
    record Pdf(
        String url,
        String outputBaseName,
        List<PageRange> pages
    ) implements DocumentSource {
        
        public Pdf {
            pages = List.copyOf(pages);
        }
        
        // The whole document
        public Pdf(String url, String outputBaseName) {
            this(url, outputBaseName, List.of());
        }
        
        // In the form OpenDataLoader PDF reads, such as "145-226,307-354"; empty for the whole document
        public String pageSelection() {
            return pages.stream().map(PageRange::selection).collect(Collectors.joining(","));
        }
    }
    
    // Physical pages of the PDF file counted from 1, not the page numbers printed on the pages
    record PageRange(
        int first,
        int last
    ) {
        
        public PageRange {
            if (first < 1 || last < first) {
                throw new IllegalArgumentException("Invalid page range: " + first + "-" + last + ". Pages are counted from 1 and the last page must not come before the first.");
            }
        }
        
        String selection() {
            return first + "-" + last;
        }
    }
    
    // Web pages, whose URLs are listed one per line in a resource, joined and saved as <outputFileName>
    record WebPages(
        String urlListResource,
        String outputFileName
    ) implements DocumentSource {
    }
}
