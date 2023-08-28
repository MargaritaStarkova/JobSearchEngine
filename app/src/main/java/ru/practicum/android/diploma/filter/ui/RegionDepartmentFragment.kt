package ru.practicum.android.diploma.filter.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentRegionDepartmentBinding
import ru.practicum.android.diploma.util.viewBinding


class RegionDepartmentFragment : Fragment(R.layout.fragment_region_department) {
    private val binding by viewBinding<FragmentRegionDepartmentBinding>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}