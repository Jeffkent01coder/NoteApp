package com.carolmusyoka.noteapp.ui.note.notes

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.carolmusyoka.noteapp.R
import com.carolmusyoka.noteapp.databinding.NotesFragmentBinding
import com.carolmusyoka.noteapp.data.room.model.Notes
import com.carolmusyoka.noteapp.utils.hide
import com.carolmusyoka.noteapp.utils.show
import kotlinx.android.synthetic.main.item_post_notes.*

class NotesFragment : Fragment(R.layout.notes_fragment) {
    private val viewModel: NotesViewModel by activityViewModels()
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var _binding: NotesFragmentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        _binding = NotesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setUpRV()
        // onclick navigate to add notes
        binding.btnAddNotes.setOnClickListener {
            findNavController().navigate(R.id.action_notesFragment_to_addNotesFragment)
        }

        // observer data change for saved notes
        lifecycleScope.launch {
            viewModel.getSavedNotes().observe(viewLifecycleOwner) { notes ->
                onNotesLoaded(notes)
            }
        }

        // onclick navigate to add notes
        notesAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("notes", it)
            }
            findNavController().navigate(
                R.id.action_notesFragment_to_notesDetailsFragment,
                bundle
            )
        }

        // init item touch callback for swipe action
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // get item position & delete notes
                val position = viewHolder.adapterPosition
                val notes = notesAdapter.differ.currentList[position]
                viewModel.deleteNotes(
                        notes.id,
                        notes.title,
                        notes.description,
                        notes.character,
                        notes.date
                )
                Snackbar.make(view, getString(R.string.note_deleted_msg), Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(getString(R.string.undo)) {
                            viewModel.insertNotes(
                                    notes.title,
                                    notes.description,
                                    notes.character,
                                    notes.date
                            )
                        }
                        show()
                    }
            }
        }

        // attach swipe callback to rv
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.notesRv)
        }
    }

    private fun onNotesLoaded(notes: List<Notes>) {
        binding.emptyStateLayout.run { if (notes.isNullOrEmpty()) show() else hide() }
        notesAdapter.differ.submitList(notes)
    }

    private fun setUpRV() {
        notesAdapter = NotesAdapter()
        binding.notesRv.apply {
            adapter = notesAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ui_menu, menu)

        // Set the item state
        lifecycleScope.launch {
            val isChecked = viewModel.getUIMode.first()
            val item = menu.findItem(R.id.action_night_mode)
            item.isChecked = isChecked
            setUIMode(item, isChecked)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.action_night_mode -> {
                item.isChecked = !item.isChecked
                setUIMode(item, item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUIMode(item: MenuItem, isChecked: Boolean) {
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            viewModel.saveToDataStore(true)
            item.setIcon(R.drawable.ic_night)

        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            viewModel.saveToDataStore(false)
            item.setIcon(R.drawable.ic_day)

        }
    }
}
