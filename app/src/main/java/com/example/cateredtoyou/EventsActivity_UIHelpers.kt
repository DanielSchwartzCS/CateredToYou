package com.example.cateredtoyou

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.cateredtoyou.R

/**
 * Utility class for managing UI dialogs and common utility functions for EventsActivity.
 * This class encapsulates dialog creation, animations, spinner updates, and progress bar management.
 */
class EventsActivity_UIHelpers(private val context: Context) {

    /**
     * Displays a dialog to add a new client.
     * Includes fields for first name and last name input.
     */
    fun showAddClientDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_addclient, null)

        MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setTitle(R.string.add_client_title)
            .setPositiveButton(R.string.add) { dialog, _ ->
                val firstname = dialogView.findViewById<EditText>(R.id.first_name).text.toString()
                val lastname = dialogView.findViewById<EditText>(R.id.last_name).text.toString()
                // TODO: Add logic to handle adding the client
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Displays a dialog for selecting staff members.
     * Placeholder method; needs specific implementation.
     */
    fun showStaffSelectionDialog() {
        // TODO: Implement staff selection dialog logic
    }

    /**
     * Displays a date picker dialog to select a date.
     * Placeholder method; needs specific implementation.
     */
    fun showDatePickerDialog() {
        // TODO: Implement date picker dialog logic
    }

    /**
     * Displays a time picker dialog to select a time.
     * Placeholder method; needs specific implementation.
     */
    fun showTimePickerDialog() {
        // TODO: Implement time picker dialog logic
    }

    /**
     * Displays an error message using a toast.
     * @param message The error message to display.
     */
    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a success message using a toast.
     * @param message The success message to display.
     */
    fun showSuccess(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Shows a progress bar by making it visible.
     * @param progressBar The progress bar view to show.
     */
    fun showProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Hides a progress bar by making it invisible.
     * @param progressBar The progress bar view to hide.
     */
    fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }

    /**
     * Updates a spinner with a new list of clients.
     * @param spinner The spinner to update.
     * @param clientList The list of clients to populate the spinner.
     */
    fun updateClientSpinner(spinner: Spinner, clientList: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, clientList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    /**
     * Selects the latest client in the spinner by setting the selection to the last item.
     * @param spinner The spinner to update.
     * @param clientList The list of clients in the spinner.
     */
    fun selectLatestClient(spinner: Spinner, clientList: List<String>) {
        spinner.setSelection(clientList.size - 1)
    }

    /**
     * Performs a fade-in animation on a view.
     * @param view The view to animate.
     */
    fun fadeInAnimation(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 300 // Animation duration in milliseconds
            fillAfter = true
        }
        view.startAnimation(fadeIn)
    }

    /**
     * Performs a fade-out animation on a view.
     * @param view The view to animate.
     */
    fun fadeOutAnimation(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 300 // Animation duration in milliseconds
            fillAfter = true
        }
        view.startAnimation(fadeOut)
    }
}
