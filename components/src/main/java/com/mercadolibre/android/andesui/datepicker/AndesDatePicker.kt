package com.mercadolibre.android.andesui.datepicker

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CalendarView
import com.mercadolibre.android.andesui.R
import com.mercadolibre.android.andesui.button.AndesButton
import com.mercadolibre.android.andesui.datepicker.factory.AndesDatePickerAttrParser
import com.mercadolibre.android.andesui.datepicker.factory.AndesDatePickerAttrs
import com.mercadolibre.android.andesui.datepicker.factory.AndesDatePickerConfiguration
import com.mercadolibre.android.andesui.datepicker.factory.AndesDatePickerConfigurationFactory
import kotlinx.android.synthetic.main.andes_layout_datepicker.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min

class AndesDatePicker : ConstraintLayout {

    interface ApplyDatePickerClickListener {
        fun onDateApply(date: Calendar)
    }
    var listener: ApplyDatePickerClickListener? = null
    /**
     * Getter and setter for [text].
     **/
    var text: String?
        get() = andesDatePickerAttrs.andesDatePickerText
        set(value) {
            andesDatePickerAttrs = andesDatePickerAttrs.copy(andesDatePickerText = value)
        }
    /**
     * Getter and setter for [minDate].
     */
    var minDate: String?
        get() = andesDatePickerAttrs.andesDatePickerMinDate
        set(value) {
            andesDatePickerAttrs = andesDatePickerAttrs.copy(andesDatePickerMinDate = value)
            setupComponents(createConfig())
        }
    /**
     * Getter and setter for [maxDate].
     */
    var maxDate: String?
        get() = andesDatePickerAttrs.andesDatePickerMaxDate
        set(value) {
            andesDatePickerAttrs = andesDatePickerAttrs.copy(andesDatePickerMaxDate = value)
            setupComponents(createConfig())
        }
    /**
     * Getter and setter for [btnVisibility].
     */
    var btnVisibility: Boolean? = null
        get() = andesDatePickerAttrs.andesBtnVisibility
    private lateinit var andesDatePickerAttrs: AndesDatePickerAttrs
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }
    constructor(
            context: Context,
            text: String? = TEXT_DEFAULT,
            minDate: String? = TEXT_DEFAULT,
            maxDate: String? = TEXT_DEFAULT,
            btnVisibility: Boolean? = null
    ) : super(context) {
        initAttrs(text, minDate, maxDate, btnVisibility)
    }
    /**
     * Sets the proper [config] for this component based on the [attrs] received via XML.
     *
     * @param attrs attributes from the XML.
     */
    private fun initAttrs(attrs: AttributeSet?) {
        andesDatePickerAttrs = AndesDatePickerAttrParser.parse(context, attrs)
        val config = AndesDatePickerConfigurationFactory.create(andesDatePickerAttrs)
        setupComponents(config)
    }

    private fun initAttrs(
            text: String?,
            minDate: String?,
            maxDate: String?,
            btnVisibility: Boolean?
    ) {
        andesDatePickerAttrs = AndesDatePickerAttrs(text,minDate,maxDate, btnVisibility)
        val config = AndesDatePickerConfigurationFactory.create(andesDatePickerAttrs)
        setupComponents(config)
    }

    /**
     * Responsible for setting up all properties of each component that is part of this andesDatePicker.
     * Is like a choreographer 😉
     */
    private fun setupComponents(config: AndesDatePickerConfiguration) {
        initComponents()
        setupViewId()
        config.minDate?.toLong()?.let { setupMinDate(it) }
        config.maxDate?.toLong()?.let { setupMaxDate(it) }
        config.text.let{setupButtonText(it)}
        config.btnVisibility.let { setupBtnVisibility(it) }
    }

    /**
     * Creates all the views that are part of this andesDatePicker.
     * After a view is created then a view id is added to it.
     */
    private fun initComponents() {
        LayoutInflater.from(context).inflate(R.layout.andes_layout_datepicker, this)
        onCheckedChangeListener(andesBtnSelectDate)
    }

    /**
     * Sets a view id to this andesDatePicker.
     */
    private fun setupViewId() {
        if (id == NO_ID) { // If this view has no id
            id = View.generateViewId()
        }
    }

    private fun validateDateToCurrentDate(date: Date): Int {
        return getNow().compareTo(date)
    }

    private fun getNow():Date{
        return convertStringToDate(SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time),"dd/MM/yyyy" )
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun convertStringToDate(time: String, format:String) : Date{
        @Suppress("NAME_SHADOWING")
        val format = SimpleDateFormat(format)
        val date = format.parse(time)
        return date
    }

    private fun _setMinDate(minDate: Long){
        val minDateDate :Date = Date(minDate)
        val dateDiference = validateDateToCurrentDate(minDateDate)
        var cumple :Boolean = true
        if (calendarView.maxDate!=null){
            cumple = minDateDate.time < calendarView.maxDate
        }
        if (validateDateToCurrentDate(minDateDate) == 0){
            calendarView.minDate = minDate
        }else{
            if (dateDiference < 0 && cumple){
                calendarView.minDate = minDate
            }else{
                Log.i("app","la fecha minima tiene que ser minima a la fecha maxima")
            }
        }
    }

    private fun _setMaxDate(maxDate: Long){
        val maxDateDate :Date = Date(maxDate)
        val dateDiference = validateDateToCurrentDate(maxDateDate)
        var cumple :Boolean = true
        if (calendarView.minDate!=null){
            cumple = maxDateDate.time > calendarView.minDate
        }
        if (validateDateToCurrentDate(maxDateDate) == 0){
            calendarView.maxDate = maxDate
        }else{
            if (dateDiference < 0 && cumple){
                calendarView.maxDate = maxDate
            }else{
                Log.i("app","la fecha maxima tiene que ser mayor a la fecha minima y mayor a la fecha actual")
            }
        }
    }

    fun setupMinDate(minDate: Long) {
        _setMinDate(minDate)
    }

    fun setupMinDate(minDate: Date) {
        _setMinDate(minDate.time)
    }

    fun setupMinDate(minDate : String, format:String){
        _setMinDate(convertStringToDate(minDate, format).time)
    }

    fun setupMaxDate(maxDate: Long) {
        _setMaxDate(maxDate)
    }

    fun setupMaxDate(maxDate: Date) {
        _setMaxDate(maxDate.time)
    }

    fun setupMaxDate(maxDate : String, format:String){
        _setMaxDate(convertStringToDate(maxDate, format).time)
    }

    fun setupButtonText(text: String?){
        andesBtnSelectDate.text = text
    }

    fun setupBtnVisibility(btnVisibility: Boolean?) {
        if (btnVisibility == true){
            andesBtnSelectDate.visibility = View.VISIBLE
        }else{
            andesBtnSelectDate.visibility = View.GONE
        }
    }

    private fun onCheckedChangeListener(andesBtnSelectDate: AndesButton) {
        val calendar = Calendar.getInstance()
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year,month,dayOfMonth)
            if(andesBtnSelectDate.visibility == View.GONE){
                listener?.onDateApply(calendar)
            }
        }
       andesBtnSelectDate.setOnClickListener {
            listener?.onDateApply(calendar)
        }
    }

    private fun createConfig() = AndesDatePickerConfigurationFactory.create(andesDatePickerAttrs)
    fun setDateListener (listener: ApplyDatePickerClickListener){
        this.listener = listener
    }
    companion object {
        private val TEXT_DEFAULT = null
    }
}
