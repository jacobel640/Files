package com.example.files.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.example.files.viewmodels.FilesViewModel

class FilesFragment : FragmentBase(FragmentType.FILES) {
    private lateinit var filesViewModel: FilesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.parent = com.example.files.Statics.folder
        filesViewModel = ViewModelProvider(this)[FilesViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        FilesScreen(
                            viewModel = filesViewModel,
                            onNavigateBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                            onNavigateToFolder = { file -> com.example.files.Statics.openFolder(java.io.File(file.path)) },
                            onOpenFile = { file -> com.example.files.Statics.openFile(file, requireContext()) }
                        )
                    }
                }
            }
        }
    }

    override fun onCreateView(v: View?) {}
    
    override fun loadList() {
        filesViewModel.refreshList(requireContext())
    }

    override fun refresh() {
        filesViewModel.refreshList(requireContext())
    }

    override fun notVisible(): Boolean {
        return !com.example.files.Statics.isVisible(com.example.files.Statics.TAG_FOLDER)
    }

    // Override to prevent NPE on binding since we completely replaced the XML view
    override fun animate() {}
    override fun refreshRecyclerPadding(addSpace: Boolean) {}
    override fun refreshGrid() {}
    override fun refreshActionsList() {}
    override fun setListeners() {}
    
    override fun setListListener() {
        com.example.files.MainActivity.instance?.addMultiSelectedChangeListener(object : com.example.files.listeners.OnMultiSelectedChange {
            override fun onMultiSelectedChange(multiSelected: Boolean) {
                if (!multiSelected) {
                    filesViewModel.clearSelection()
                }
            }
            override fun onRefresh() {}
            override fun onRefreshActionsList() {}
        })
    }
}
