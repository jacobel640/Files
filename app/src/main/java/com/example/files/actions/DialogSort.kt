package com.example.files.actions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.icu.text.Collator
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.files.JFileAdapter
import com.example.files.MainActivity.editor
import com.example.files.MainActivity.instance
import com.example.files.R
import com.example.files.Statics.order
import com.example.files.Statics.sort
import com.example.files.models.JFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class DialogSort : BottomSheetDialog {

    private val activity: Activity
    private var jFileList: ArrayList<JFile>? = null
    private var jFileAdapter: JFileAdapter? = null
    private val isNormalSize: Boolean

    constructor() : super(instance) {
        this.activity = instance
        this.isNormalSize = isNormalScreen(activity)
    }

    constructor(jFileAdapter: JFileAdapter) : super(instance) {
        this.activity = instance
        this.jFileAdapter = jFileAdapter
        this.jFileList = jFileAdapter.jFileList
        this.isNormalSize = isNormalScreen(activity)
        createDialogSort()
    }

    private fun createDialogSort() {
        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setContent {
                MaterialTheme {
                    SortDialogContent(
                        initialSort = sort,
                        initialOrder = order,
                        onApply = { selectedSort, selectedOrder ->
                            editor.putInt("SORT", selectedSort).apply()
                            editor.putInt("ORDER", selectedOrder).apply()
                            dismiss()
                            Handler(Looper.getMainLooper()).post { sort() }
                        },
                        onCancel = { dismiss() }
                    )
                }
            }
        }
        setContentView(composeView)
    }

    private fun isNormalScreen(context: Context): Boolean {
        return context.resources.configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sort() {
        if (!jFileList.isNullOrEmpty()) {
            compare(jFileList!!, sort, order == 1)
        }
        jFileAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortAndNotify(jFileAdapter: JFileAdapter) {
        if (jFileAdapter.itemCount > 0) {
            compare(jFileAdapter.jFileList, sort, order == 1)
        }
        jFileAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun compareAndNotify(jFileAdapter: JFileAdapter, sort: Int, reverse: Boolean) {
        compare(jFileAdapter.jFileList, sort, reverse)
        jFileAdapter.notifyDataSetChanged()
    }

    companion object {
        private val collator = Collator.getInstance(Locale.getDefault())

        @JvmStatic
        fun sort(jFileList: ArrayList<JFile>?) {
            if (!jFileList.isNullOrEmpty()) {
                compare(jFileList, sort, order == 1)
            }
        }

        @JvmStatic
        fun compare(jFiles: ArrayList<JFile>, sortType: Int, reverse: Boolean) {
            when (sortType) {
                0 -> if (reverse) jFiles.sortWith(Comparator { f1, f2 -> compareNameReverse(f1, f2) }) else jFiles.sortWith(Comparator { f1, f2 -> compareName(f1, f2) })
                1 -> if (reverse) jFiles.sortWith(Comparator { f1, f2 -> compareSizeReverse(f1, f2) }) else jFiles.sortWith(Comparator { f1, f2 -> compareSize(f1, f2) })
                2 -> if (reverse) jFiles.sortWith(Comparator { f1, f2 -> compareDateReverse(f1, f2) }) else jFiles.sortWith(Comparator { f1, f2 -> compareDate(f1, f2) })
                3 -> if (reverse) jFiles.sortWith(Comparator { f1, f2 -> compareTypeReverse(f1, f2) }) else jFiles.sortWith(Comparator { f1, f2 -> compareType(f1, f2) })
            }
        }

        @JvmStatic
        fun compareRecentFile(jFiles: ArrayList<JFile>) {
            jFiles.sortWith(Comparator { f1, f2 -> compareRecentFile(f1, f2) })
        }

        @JvmStatic
        fun compareRecentFile(file1: JFile, file2: JFile): Int {
            return java.lang.Long.compare(file2.lastModified(), file1.lastModified())
        }

        @JvmStatic
        fun compareName(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            return compare(file1.nameTLC, file2.nameTLC)
        }

        @JvmStatic
        fun compareNameReverse(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            return compare(file2.nameTLC, file1.nameTLC)
        }

        @JvmStatic
        fun compareSize(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            val sizeOrder = java.lang.Long.compare(file1.size, file2.size)
            return if (sizeOrder != 0) sizeOrder else compare(file1.nameTLC, file2.nameTLC)
        }

        @JvmStatic
        fun compareSizeReverse(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            val sizeOrder = java.lang.Long.compare(file2.size, file1.size)
            return if (sizeOrder != 0) sizeOrder else compare(file1.nameTLC, file2.nameTLC)
        }

        @JvmStatic
        fun compareDate(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            return java.lang.Long.compare(file1.lastModified(), file2.lastModified())
        }

        @JvmStatic
        fun compareDateReverse(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            return java.lang.Long.compare(file2.lastModified(), file1.lastModified())
        }

        @JvmStatic
        fun compareType(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            val extOrder = file1.extension.compareTo(file2.extension)
            return if (extOrder != 0) extOrder else compare(file1.nameTLC, file2.nameTLC)
        }

        @JvmStatic
        fun compareTypeReverse(file1: JFile, file2: JFile): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1
            val extOrder = file2.extension.compareTo(file1.extension)
            return if (extOrder != 0) extOrder else compare(file1.nameTLC, file2.nameTLC)
        }

        @JvmStatic
        fun compare(file1: JFile, file2: JFile, sort: Int, reverse: Boolean): Int {
            if (file1.isDirectory && !file2.isDirectory) return -1
            else if (!file1.isDirectory && file2.isDirectory) return 1

            return when (sort) {
                0 -> if (reverse) compare(file2.nameTLC, file1.nameTLC) else compare(file1.nameTLC, file2.nameTLC)
                1 -> {
                    val sizeOrder = if (reverse) java.lang.Long.compare(file2.size, file1.size) else java.lang.Long.compare(file1.size, file2.size)
                    if (sizeOrder != 0) sizeOrder else compare(file1.nameTLC, file2.nameTLC)
                }
                2 -> if (reverse) java.lang.Long.compare(file2.lastModified(), file1.lastModified()) else java.lang.Long.compare(file1.lastModified(), file2.lastModified())
                3 -> {
                    val extOrder = if (reverse) file2.extension.compareTo(file1.extension) else file1.extension.compareTo(file2.extension)
                    if (extOrder != 0) extOrder else compare(file1.nameTLC, file2.nameTLC)
                }
                else -> 0
            }
        }

        private fun isDigit(ch: Char): Boolean = ch in '0'..'9'

        private fun getChunk(s: String, sLength: Int, markerArg: Int): String {
            var marker = markerArg
            val chunk = StringBuilder()
            var c = s[marker]
            chunk.append(c)
            marker++
            if (isDigit(c)) {
                while (marker < sLength) {
                    c = s[marker]
                    if (!isDigit(c)) break
                    chunk.append(c)
                    marker++
                }
            } else {
                while (marker < sLength) {
                    c = s[marker]
                    if (isDigit(c)) break
                    chunk.append(c)
                    marker++
                }
            }
            return chunk.toString()
        }

        @JvmStatic
        fun compare(s1: String?, s2: String?): Int {
            if (s1 == null || s2 == null) return 0
            var thisMarker = 0
            var thatMarker = 0
            val s1Length = s1.length
            val s2Length = s2.length

            while (thisMarker < s1Length && thatMarker < s2Length) {
                val thisChunk = getChunk(s1, s1Length, thisMarker)
                thisMarker += thisChunk.length

                val thatChunk = getChunk(s2, s2Length, thatMarker)
                thatMarker += thatChunk.length

                var result: Int
                if (isDigit(thisChunk[0]) && isDigit(thatChunk[0])) {
                    val thisChunkLength = thisChunk.length
                    result = thisChunkLength - thatChunk.length
                    if (result == 0) {
                        for (i in 0 until thisChunkLength) {
                            result = thisChunk[i] - thatChunk[i]
                            if (result != 0) return result
                        }
                    }
                } else {
                    result = collator.compare(thisChunk, thatChunk)
                }

                if (thisChunk.startsWith(".") && !thatChunk.startsWith(".")) result = 1
                else if (!thisChunk.startsWith(".") && thatChunk.startsWith(".")) result = -1

                if (result != 0) return result
            }
            return s1Length - s2Length
        }
    }
}

@Composable
fun SortDialogContent(
    initialSort: Int,
    initialOrder: Int,
    onApply: (Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    var selectedSort by remember { mutableIntStateOf(initialSort) }
    var selectedOrder by remember { mutableIntStateOf(initialOrder) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.sort_by),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 0.dp)
                .padding(bottom = 20.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val sortOptions = listOf(
                0 to stringResource(R.string.name),
                1 to stringResource(R.string.size),
                2 to stringResource(R.string.sort_date),
                3 to stringResource(R.string.sort_type)
            )

            sortOptions.forEachIndexed { index, (id, label) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOptions.size),
                    onClick = { selectedSort = id },
                    selected = selectedSort == id
                ) {
                    Text(text = label)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.order),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 10.dp, bottom = 5.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            val orderOptions = listOf(
                0 to stringResource(R.string.ascending_order),
                1 to stringResource(R.string.descending_order)
            )

            orderOptions.forEachIndexed { index, (id, label) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = orderOptions.size),
                    onClick = { selectedOrder = id },
                    selected = selectedOrder == id
                ) {
                    Text(text = label)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.cancel), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .padding(horizontal = 5.dp)
            )

            TextButton(
                onClick = { onApply(selectedSort, selectedOrder) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.apply), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
