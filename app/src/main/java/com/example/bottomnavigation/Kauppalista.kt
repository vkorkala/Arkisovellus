package com.example.bottomnavigation

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import java.io.File
import android.text.TextPaint
import androidx.core.content.ContextCompat

class Kauppalista : Fragment() {

    // Luodaan muuttujat
    private lateinit var layoutUusi: ConstraintLayout
    private lateinit var editTieto: EditText
    private lateinit var tvTulostieto: TextView
    private lateinit var btnLisaa: Button
    private lateinit var btnPoista: Button

    // Määritellään tallennustiedosto
    private val tiedostonNimi = "kauppalista.txt"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ladataan fragmentin layout
        val view = inflater.inflate(R.layout.fragment_kauppalista, container, false)

        // Yhdistetään muuttujat käyttöliittymään
        layoutUusi = view.findViewById(R.id.layoutUusi)
        editTieto = view.findViewById(R.id.editTieto)
        tvTulostieto = view.findViewById(R.id.tvTulostieto)
        tvTulostieto.movementMethod = LinkMovementMethod.getInstance()
        btnLisaa = view.findViewById(R.id.btnLisaa)
        btnPoista = view.findViewById(R.id.btnPoista)

        // Lataa aikaisemmin tallennetut rivit kun fragmentti avataan
        lataaTallennetutRivit()

        // Asetetaan painikkeille kuuntelijat
        btnLisaa.setOnClickListener {
            lisaaRivi()
        }

        btnPoista.setOnClickListener {
            tyhjennaLista()
        }

        return view
    }

    // Lisää uuden rivin kauppalistaan
    private fun lisaaRivi() {
        val uusiRivi = editTieto.text.toString()
        if (uusiRivi.isNotEmpty()) {
            val clickableText = createClickableText(uusiRivi)
            val spannableBuilder = SpannableStringBuilder(tvTulostieto.text)
            spannableBuilder.append(clickableText)
            tvTulostieto.text = spannableBuilder
            editTieto.text.clear()

            // Tallenna rivit aina uuden rivin lisäämisen yhteydessä
            tallennaRivit()
        }
    }

    // Luo klikattavan poista-tekstin riville
    private fun createClickableText(rivi: String): SpannableString {
        val spannable = SpannableString("$rivi    Poista\n")
        val clickableSpan = object : ClickableSpan() {

            // Poistaa rivin painettaessa
            override fun onClick(widget: View) {
                poistaRivi(rivi)
            }

            // Muokkaa Poista-linkin ulkoasua
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)

                // Poistetaan alleviivaus
                ds.isUnderlineText = false

                // Vaihdetaan tekstin väri terrakotaksi
                ds.color = ContextCompat.getColor(
                    requireContext(),
                    R.color.terracotta
                )
            }
        }
        spannable.setSpan(clickableSpan, rivi.length, spannable.length, 0)
        return spannable
    }

    // Poistaa rivin kauppalistasta
    private fun poistaRivi(rivi: String) {
        val rivit = tvTulostieto.text.toString().split("\n").filter { it.isNotEmpty() }.toMutableList()
        val index = rivit.indexOfFirst { it.startsWith(rivi) }
        if (index != -1) {
            rivit.removeAt(index)
        }
        val spannableBuilder = SpannableStringBuilder()
        for (item in rivit) {
            spannableBuilder.append(createClickableText(item.removeSuffix("    Poista")))
        }
        tvTulostieto.text = spannableBuilder

        // Tallenna rivit aina kun rivi poistetaan
        tallennaRivit()
    }

    // Tallentaa kauppalistan rivit tiedostoon
    private fun tallennaRivit() {
        val rivit = tvTulostieto.text.toString()
        context?.openFileOutput(tiedostonNimi, Context.MODE_PRIVATE).use {
            it?.write(rivit.toByteArray())
        }
    }

    // Lataa tallennetut kauppalistan rivit tiedostosta
    private fun lataaTallennetutRivit() {
        val tiedosto = File(context?.filesDir, tiedostonNimi)
        if (tiedosto.exists()) {
            val rivit = tiedosto.readText()
            val spannableBuilder = SpannableStringBuilder()
            val rivitList = rivit.split("\n")
            for (rivi in rivitList) {
                if (rivi.isNotEmpty()) {
                    val spannable = createClickableText(rivi.removeSuffix("    Poista"))
                    spannableBuilder.append(spannable)
                }
            }
            tvTulostieto.text = spannableBuilder
        }
    }

    // Tyhjentää kauppalistan
    private fun tyhjennaLista() {
        tvTulostieto.text = ""
        context?.openFileOutput(tiedostonNimi, Context.MODE_PRIVATE).use {
            it?.write("".toByteArray())
        }
        Toast.makeText(requireContext(), "Lista tyhjennetty", Toast.LENGTH_SHORT).show()
    }
}
