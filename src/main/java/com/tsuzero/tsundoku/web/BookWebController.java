package com.tsuzero.tsundoku.web;

import com.tsuzero.tsundoku.book.BookService;
import com.tsuzero.tsundoku.book.LibraryAvailabilityRepository;
import com.tsuzero.tsundoku.finna.FinnaSyncJob;
import com.tsuzero.tsundoku.goodreads.GoodreadsSyncService;
import com.tsuzero.tsundoku.sync.SyncLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BookWebController {

    private final BookService bookService;
    private final LibraryAvailabilityRepository availabilityRepository;
    private final FinnaSyncJob finnaSyncJob;
    private final GoodreadsSyncService goodreadsSyncService;
    private final SyncLogRepository syncLogRepository;

    public BookWebController(BookService bookService,
                             LibraryAvailabilityRepository availabilityRepository,
                             FinnaSyncJob finnaSyncJob,
                             GoodreadsSyncService goodreadsSyncService,
                             SyncLogRepository syncLogRepository) {
        this.bookService = bookService;
        this.availabilityRepository = availabilityRepository;
        this.finnaSyncJob = finnaSyncJob;
        this.goodreadsSyncService = goodreadsSyncService;
        this.syncLogRepository = syncLogRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String books(@RequestParam(required = false) String available,
                        @RequestParam(required = false) String sort,
                        @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                        Model model) {
        List<BookViewModel> books = bookService.listBooks(available, sort)
                .stream().map(BookViewModel::of).toList();
        model.addAttribute("books", books);
        model.addAttribute("available", available);
        model.addAttribute("sort", sort);

        if (hxRequest != null) {
            return "fragments/ui :: bookList";
        }
        model.addAttribute("syncLogs", syncLogRepository.findLatestPerType());
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        var book = bookService.findById(id).orElseThrow();
        model.addAttribute("book", BookViewModel.of(book));
        return "books/detail";
    }

    @PostMapping("/books/{id}/availability/refresh")
    public String refreshAvailability(@PathVariable Long id, Model model) {
        var avail = availabilityRepository.findByBookId(id).orElseThrow();
        finnaSyncJob.checkSingle(avail);
        var book = bookService.findById(id).orElseThrow();
        model.addAttribute("book", BookViewModel.of(book));
        return "fragments/ui :: availabilitySection";
    }

    @PostMapping("/sync/goodreads")
    public String syncGoodreads(Model model) {
        goodreadsSyncService.sync();
        model.addAttribute("syncLogs", syncLogRepository.findLatestPerType());
        return "fragments/ui :: syncStatus";
    }
}
