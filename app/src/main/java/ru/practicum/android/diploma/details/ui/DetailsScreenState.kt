package ru.practicum.android.diploma.details.ui

import android.widget.Toast
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.FragmentDetailsBinding
import ru.practicum.android.diploma.details.domain.models.VacancyFullInfo
import ru.practicum.android.diploma.util.setImage

sealed interface DetailsScreenState {
    fun render(binding: FragmentDetailsBinding)
    object Empty : DetailsScreenState{
        override fun render(binding: FragmentDetailsBinding) = Unit
    }
    data class Content(val vacancy: VacancyFullInfo) : DetailsScreenState{
        override fun render(binding: FragmentDetailsBinding) {
            with(binding) {
                val tvSchedule = vacancy.employment + ". " + vacancy.schedule
                val formattedDescription =
                    HtmlCompat.fromHtml(vacancy.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvKeySkills.text = vacancy.keySkills
                tvContactsName.text = vacancy.contactName
                tvContactsEmail.text = vacancy.contactEmail
                tvContactsPhone.text =
                    if (vacancy.contactPhones.isEmpty()) { binding.root.context.getString(R.string.no_info) }
                    else { vacancy.contactPhones.joinToString("\n") }
                tvExperience.text = vacancy.experience
                tvScheduleEmployment.text = tvSchedule
                tvDescription.text = formattedDescription
                tvNameOfVacancy.text = vacancy.title
                tvSalary.text = vacancy.salary
                tvNameOfCompany.text = vacancy.company
                tvArea.text = vacancy.area
                imageView.setImage(vacancy.logo, R.drawable.ic_placeholder_company)
            }
        }
    }

    data class Offline(val message: String) : DetailsScreenState {
        override fun render(binding: FragmentDetailsBinding) {
            Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    data class Error(val message: String) : DetailsScreenState {
        override fun render(binding: FragmentDetailsBinding) {
            Toast.makeText(binding.root.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    class PlayHeartAnimation(private val isInFavs: Boolean, val scope: CoroutineScope) :
        DetailsScreenState {
        override fun render(binding: FragmentDetailsBinding) {
            scope.launch {
                if (isInFavs) {
                    binding.lottieHeart.speed = STRAIGHT_ANIMATION_SPEED
                    binding.lottieHeart.playAnimation()
                } else {
                    binding.lottieHeart.speed = REVERS_ANIMATION_SPEED
                    binding.lottieHeart.playAnimation()
                }
            }
        }
    }
}
const val STRAIGHT_ANIMATION_SPEED = 1f
const val REVERS_ANIMATION_SPEED = -1f