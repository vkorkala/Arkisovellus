package com.example.bottomnavigation

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File
import android.text.TextPaint
import androidx.core.content.ContextCompat

class Muistio : Fragment() {

    // Luodaan muuttujat
    private lateinit var editMuistio: EditText
    private lateinit var tvMuistio: TextView
    private lateinit var btnLisaa: Button
    private lateinit var btnPoistaKaikki: Button

    // Määritellään tallennustiedosto
    private val tiedostonNimi = "muistio.txt"
    // Muokattavan merkinnän indeksi
    private var muokattavaIndex: Int? = null
    // Lista muistion merkinnöistä
    private val merkinnat = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ladataan fragmentin layout
        val view = inflater.inflate(R.layout.fragment_muistio, container, false)

        // Yhdistetään muuttujat käyttöliittymään
        editMuistio = view.findViewById(R.id.editMuistio)
        tvMuistio = view.findViewById(R.id.tvMuistio)
        tvMuistio.movementMethod = CustomLinkMovementMethod()
        btnLisaa = view.findViewById(R.id.btnLisaa)
        btnPoistaKaikki = view.findViewById(R.id.btnPoistaKaikki)

        // Ladataan aikaisemmin tallennetut merkinnät
        lataaTallennetutMerkinnat()

        // Asetetaan painikkeille kuuntelijat
        btnLisaa.setOnClickListener {
            if (muokattavaIndex == null) {
                lisaaMuistio()
            } else {
                paivitaMuistio()
            }
        }

        btnPoistaKaikki.setOnClickListener {
            tyhjennaMuistio()
        }

        return view
    }

    // Funktio, joka lisää uuden merkinnän
    private fun lisaaMuistio() {
        val uusiMerkinta = editMuistio.text.toString()
        if (uusiMerkinta.isNotEmpty()) {
            merkinnat.add(uusiMerkinta)
            paivitaTextView()
            editMuistio.text.clear()
            tallennaMerkinnat()
        }
    }

    // Päivittää aikaisemmin tallennettuun merkintään tehdyt muutokset
    private fun paivitaMuistio() {
        val uusiMerkinta = editMuistio.text.toString()
        if (uusiMerkinta.isNotEmpty()) {
            merkinnat[muokattavaIndex!!] = uusiMerkinta
            muokattavaIndex = null
            paivitaTextView()
            editMuistio.text.clear()
            tallennaMerkinnat()
        } else {
            poistaMuistio(muokattavaIndex!!)
            editMuistio.text.clear()
            muokattavaIndex = null
            tallennaMerkinnat()
        }
    }

    // Poistaa merkinnän
    private fun poistaMuistio(index: Int) {
        merkinnat.removeAt(index)
        paivitaTextView()
        tallennaMerkinnat()
    }

    // Päivittää tekstinäkymän sisällön
    private fun paivitaTextView() {
        try {
            val spannableBuilder = SpannableStringBuilder()
            for ((index, merkinta) in merkinnat.withIndex()) {
                val spannable = createClickableText(merkinta, index)
                spannableBuilder.append(spannable).append("\n") // Lisätään tyhjä rivi
            }
            tvMuistio.text = spannableBuilder
        } catch (e: Exception) {
            Log.e("Muistio", "Virhe päivitettäessä TextView: ${e.message}")
        }
    }

    // Luo klikattavat tekstit merkinnän perään
    // Luo klikattavat tekstit merkinnän perään
    private fun createClickableText(merkinta: String, index: Int): SpannableString {

        val spannable = SpannableString("$merkinta\nMuokkaa   Poista\n")

        // Klikattava Muokkaa-toiminto
        val clickableSpanMuokkaa = object : ClickableSpan() {

            override fun onClick(widget: View) {
                editMuistio.setText(merkinta)
                muokattavaIndex = index
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)

                // Poistetaan alleviivaus
                ds.isUnderlineText = false

                // Muokkaa vihreäksi
                ds.color = ContextCompat.getColor(
                    requireContext(),
                    R.color.darkgreen
                )
            }
        }

        // Klikattava Poista-toiminto
        val clickableSpanPoista = object : ClickableSpan() {

            override fun onClick(widget: View) {
                poistaMuistio(index)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)

                // Poistetaan alleviivaus
                ds.isUnderlineText = false

                // Poista terrakotaksi
                ds.color = ContextCompat.getColor(
                    requireContext(),
                    R.color.terracotta
                )
            }
        }

        // Määritellään klikattavat alueet
        spannable.setSpan(
            clickableSpanMuokkaa,
            merkinta.length + 1,
            merkinta.length + 8,
            0
        )

        spannable.setSpan(
            clickableSpanPoista,
            merkinta.length + 11,
            merkinta.length + 17,
            0
        )

        return spannable
    }

    // Tallentaa merkinnät tiedostoon
    private fun tallennaMerkinnat() {
        try {
            context?.openFileOutput(tiedostonNimi, Context.MODE_PRIVATE).use {
                it?.write(merkinnat.joinToString("\n\n").toByteArray())
            }
        } catch (e: Exception) {
            Log.e("Muistio", "Virhe tallennettaessa merkintöjä: ${e.message}")
        }
    }

    // Lataa aikaisemmin tallennetut merkinnät tiedostosta
    private fun lataaTallennetutMerkinnat() {
        try {
            val tiedosto = File(context?.filesDir, tiedostonNimi)
            if (tiedosto.exists()) {
                val rivit = tiedosto.readText().split("\n\n")
                merkinnat.clear()
                merkinnat.addAll(rivit.filter { it.isNotEmpty() })
                paivitaTextView()
            }
        } catch (e: Exception) {
            Log.e("Muistio", "Virhe ladattaessa merkintöjä: ${e.message}")
        }
    }

    // Tyhjentää muistion
    private fun tyhjennaMuistio() {
        merkinnat.clear()
        paivitaTextView()
        try {
            context?.openFileOutput(tiedostonNimi, Context.MODE_PRIVATE).use {
                it?.write("".toByteArray())
            }
        } catch (e: Exception) {
            Log.e("Muistio", "Virhe tyhjennettäessä merkintöjä: ${e.message}")
        }
    }

    // Funktio, joka estää klikkauksen skrollatessa
    private class CustomLinkMovementMethod : LinkMovementMethod() {
        private var isScrolling = false

        override fun onTouchEvent(widget: TextView, buffer: android.text.Spannable, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> isScrolling = false
                MotionEvent.ACTION_MOVE -> isScrolling = true
                MotionEvent.ACTION_UP -> if (isScrolling) return true
            }
            return super.onTouchEvent(widget, buffer, event)
        }
    }
}
