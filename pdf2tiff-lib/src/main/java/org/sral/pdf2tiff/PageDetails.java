package org.sral.pdf2tiff;

public class PageDetails {
    private final int currentPage;
    private final int totalPages;

    public PageDetails(int currentPage, int totalPages) {

        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
