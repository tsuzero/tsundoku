package com.tsuzero.tsundoku.book;

import com.tsuzero.tsundoku.finna.FinnaSyncJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final LibraryAvailabilityRepository availabilityRepository;
    private final FinnaSyncJob finnaSyncJob;

    public BookController(BookService bookService,
                          LibraryAvailabilityRepository availabilityRepository,
                          FinnaSyncJob finnaSyncJob) {
        this.bookService = bookService;
        this.availabilityRepository = availabilityRepository;
        this.finnaSyncJob = finnaSyncJob;
    }

    @GetMapping
    public List<BookDto> listBooks(
            @RequestParam(required = false) String available,
            @RequestParam(required = false) String sort) {
        return bookService.listBooks(available, sort);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
        return bookService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/availability/refresh")
    public ResponseEntity<Void> refreshAvailability(@PathVariable Long id) {
        var avail = availabilityRepository.findByBookId(id);
        if (avail.isEmpty()) return ResponseEntity.notFound().build();
        finnaSyncJob.checkSingle(avail.get());
        return ResponseEntity.ok().build();
    }
}
