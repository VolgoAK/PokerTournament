package com.volgoak.pokertournament.screens.creategame

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aigestudio.wheelpicker.WheelPicker

import com.volgoak.pokertournament.R
import com.volgoak.pokertournament.data.toReadableText
import com.volgoak.pokertournament.extensions.observeSafe
import com.volgoak.pokertournament.screens.main.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_game.*
import kotlinx.android.synthetic.main.fragment_create_game.fabStart
import kotlinx.android.synthetic.main.fragment_create_game.wheelBlindsStructure
import kotlinx.android.synthetic.main.fragment_create_game.wheelRoundTime
import kotlinx.android.synthetic.main.fragment_create_game.wheelStartBlinds
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class CreateGameFragment : Fragment() {

    companion object {
        fun newInstance() = CreateGameFragment()
    }

    private val viewModel by sharedViewModel<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWheel(wheelRoundTime)
        setupWheel(wheelBlindsStructure)
        setupWheel(wheelStartBlinds)

        wheelBlindsStructure.setOnItemSelectedListener { _, _, position ->
            viewModel.onStructureChanged(position)
        }

        fabStart.setOnClickListener {
            viewModel.onStartGameClicked(
                    wheelBlindsStructure.currentItemPosition,
                    wheelRoundTime.currentItemPosition,
                    wheelStartBlinds.currentItemPosition)
        }

        viewModel.timeListLiveData.observeSafe(this) { timeList ->
            wheelRoundTime.data = timeList.map { it.text }
        }
        viewModel.structuresLiveData.observeSafe(this) { structures ->
            wheelBlindsStructure.data = structures.map { it.name }
        }
        viewModel.blindsListLiveData.observeSafe(this) { blinds ->
            wheelStartBlinds.data = blinds.map { it.toReadableText() }
        }
    }

    //prepare wheelPicker
    private fun setupWheel(wheel: WheelPicker) {
        with(wheel) {
            setAtmospheric(true)
            itemTextSize = resources.getDimensionPixelSize(R.dimen.wheel_text_size)
            isCurved = true
            isCyclic = true
            visibleItemCount = 2
        }
    }
}
