package com.autgroup.s2025.w201.todo.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.autgroup.s2025.w201.todo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetInfo : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_ADDRESS = "address"
        private const val ARG_RATING = "rating"
        private const val ARG_STATUS = "status"

        fun newInstance(info: PlaceInfo?): BottomSheetInfo {
            val fragment = BottomSheetInfo()
            val args = Bundle()
            args.putString(ARG_NAME, info?.name)
            args.putString(ARG_ADDRESS, info?.address)
            args.putDouble(ARG_RATING, info?.rating ?: 0.0)
            args.putString(ARG_STATUS, info?.openStatus)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText = view.findViewById<TextView>(R.id.titleText)
        val addressText = view.findViewById<TextView>(R.id.addressText)
        val ratingText = view.findViewById<TextView>(R.id.ratingText)
        val openStatusText = view.findViewById<TextView>(R.id.openStatusText)

        arguments?.let {
            titleText.text = it.getString(ARG_NAME) ?: "No Name"
            addressText.text = it.getString(ARG_ADDRESS) ?: "No Address"
            ratingText.text = "⭐ ${it.getDouble(ARG_RATING, 0.0)}"
            openStatusText.text = it.getString(ARG_STATUS) ?: "Hours unknown"
        }
    }
}