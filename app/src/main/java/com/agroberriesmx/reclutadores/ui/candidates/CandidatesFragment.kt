package com.agroberriesmx.reclutadores.ui.candidates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agroberriesmx.reclutadores.R
import com.agroberriesmx.reclutadores.databinding.FragmentCandidatesBinding


class CandidatesFragment : Fragment() {

    private var _binding: FragmentCandidatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCandidatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Es una buena práctica limpiar el binding para evitar fugas de memoria
        _binding = null
    }
}