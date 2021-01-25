package es.urjc.code.daw.library.unitary;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BookRestControllerTests MockMVC")
public class BookRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("Given a regular user, When user requests all books, Then user gets all books.")
    public void getAllBooks() throws Exception {
        List<Book> books = Arrays.asList(
                new Book("Super libro",
                        "Super resumen..."),
                new Book("Super libro 2",
                        "Super resumen 2..."));

        when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/api/books/")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].title", equalTo("Super libro")))
        .andExpect(jsonPath("$[1].title", equalTo("Super libro 2")));

    }

    @Test
    @DisplayName("Given a regular user, When user requests all books but db is empty, Then user does not get all books")
    public void getAllBooksFail() throws Exception {
        when(bookService.findAll()).thenReturn(new ArrayList<>());

        mvc.perform(get("/api/books/")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Given logged user, When user creates a new book, Then is ok")
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    public void loggedUserCreatesABook() throws Exception {
        String title = "Super libro";
        String description = "Super resumen";
        Book book = new Book(title, description);
        when(bookService.save(Mockito.any(Book.class))).thenReturn(book);

        mvc.perform(post("/api/books/")
            .content(new ObjectMapper().writeValueAsString(book))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", equalTo("Super libro")))
            .andExpect(jsonPath("$.description", equalTo("Super resumen")));
    }

    @Test
    @DisplayName("Given logged user, When user deletes a book, Then is ok")
    @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
    public void loggerUserDeletesABook() throws Exception {
        Mockito.doNothing().when(bookService).delete(1);

        mvc.perform(delete("/api/books/1")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

}
