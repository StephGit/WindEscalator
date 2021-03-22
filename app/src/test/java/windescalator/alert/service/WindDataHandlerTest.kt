package windescalator.alert.service


import android.content.Context
import com.nhaarman.mockito_kotlin.mock
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import windescalator.data.entity.Alert

class WindDataHandlerTest {

    @Mock
    private val mockContext = mock<Context>()


    @Test
    fun shouldGetWindDataThun() {

        val alert = Alert("", true, "thun", "", "", 0, emptyList(), 0L)
        assertTrue(WindDataHandler(mockContext).isFiring(alert))
    }
}