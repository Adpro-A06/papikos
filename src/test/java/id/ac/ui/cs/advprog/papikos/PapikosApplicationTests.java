package id.ac.ui.cs.advprog.papikos;

import id.ac.ui.cs.advprog.papikos.wishlist.wishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class PapikosApplicationTests {

    // Menggunakan @MockBean untuk menyuntikkan bean tiruan bagi wishlistService
    @SuppressWarnings("removal")
    @MockBean
    private wishlistService wishlistService;
    
    @Test
    void contextLoads() {
        // Test ini hanya memeriksa apakah konteks aplikasi dapat termuat tanpa error
    }
}
