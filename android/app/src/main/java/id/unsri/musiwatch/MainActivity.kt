// Kerangka layar praktik P1 (BELUM dikompilasi; sesuaikan nama paket dengan proyek Anda).
// Memakai fungsi dari HttpClients.kt: buildClient, loadStations, runFlakyTest.
package id.unsri.musiwatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.unsri.musiwatch.net.buildClient
import id.unsri.musiwatch.net.loadStations
import id.unsri.musiwatch.net.runFlakyTest
import kotlinx.coroutines.launch

private const val TAG = "P1"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { PracticeScreen() } }
    }
}

private fun median(l: List<Long>): Double {
    val s = l.sorted()
    return if (s.size % 2 == 1) s[s.size / 2].toDouble()
    else (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0
}

/** Satu kali muat 50 stasiun (tombol 1). */
private suspend fun loadOnce(): String {
    val (ms, body) = loadStations(buildClient(false), limit = 50)
    Log.d(TAG, "muat1x durasi=$ms ms, body=${body.length} karakter")
    return "Muat 50 stasiun: $ms ms (${body.length} karakter)"
}

/** Muat 50 stasiun n kali dengan SATU klien (panggilan pertama membuka koneksi, sisanya memakai ulang). */
private suspend fun measure(n: Int): String {
    val client = buildClient(false)
    val d = (1..n).map { loadStations(client, limit = 50).first }
    Log.d(TAG, "durasi_ms=$d")
    val tanpaPertama = d.drop(1)
    return "Durasi (ms): $d\n" +
        "Semua $n: median ${median(d)} | maks ${d.max()}\n" +
        "Tanpa panggilan pertama: median ${median(tanpaPertama)} | maks ${tanpaPertama.max()}"
}

/** Uji /v1/flaky 20x, dengan atau tanpa retry (dipakai untuk bukti P1.3). */
private suspend fun flaky(withRetry: Boolean): String {
    val r = runFlakyTest(buildClient(withRetry), 20)
    val ok = r.count { it.ok }
    val detail = r.mapIndexed { i, c -> "${i + 1}: ${c.code ?: "ERR"} ${c.durationMs} ms" }
    Log.d(TAG, "flaky retry=$withRetry sukses=$ok gagal=${r.size - ok}")
    return "retry=$withRetry -> sukses $ok, gagal ${r.size - ok}\n" + detail.joinToString("\n")
}

@Composable
private fun PracticeScreen() {
    val scope = rememberCoroutineScope()
    var output by remember { mutableStateOf("Siap.") }
    var busy by remember { mutableStateOf(false) }

    fun run(block: suspend () -> String) {
        if (busy) return
        busy = true
        output = "Berjalan..."
        scope.launch {
            output = try { block() } catch (e: Exception) { "Error: ${e::class.simpleName}: ${e.message}" }
            busy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()          // agar tombol tidak tertutup status bar (edge-to-edge)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(enabled = !busy, onClick = { run { loadOnce() } }) { Text("Muat 50 stasiun") }
        Button(enabled = !busy, onClick = { run { measure(10) } }) { Text("Ukur 50 stasiun 10x") }
        Button(enabled = !busy, onClick = { run { flaky(false) } }) { Text("Uji /v1/flaky 20x TANPA retry") }
        Button(enabled = !busy, onClick = { run { flaky(true) } }) { Text("Uji /v1/flaky 20x DENGAN retry") }
        Text(output)
    }
}
