package com.example.hemo_conecta

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hemo_conecta.data.Hemocentro
import com.example.hemo_conecta.data.HemocentroRepository
import com.example.hemo_conecta.databinding.FragmentLocalizacaoBinding
import com.example.hemo_conecta.databinding.ItemHemocentroBinding

/**
 * Tela de localização de hemocentros. Digitar na barra de pesquisa
 * busca em /hemocentros no Firebase (via [HemocentroRepository]) e
 * mostra os resultados como cards, no lugar da imagem do mapa.
 */
class LocalizacaoFragment : Fragment() {

    private var _binding: FragmentLocalizacaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalizacaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                buscar(s?.toString().orEmpty())
            }
        })
    }

    private fun buscar(termo: String) {
        if (termo.isBlank()) {
            mostrarImagemPadrao()
            return
        }

        HemocentroRepository.buscarPorCidade(termo) { resultados ->
            // o callback do Firebase pode voltar depois da view já ter sido destruída
            if (_binding == null) return@buscarPorCidade
            exibirResultados(resultados)
        }
    }

    private fun mostrarImagemPadrao() {
        binding.imagemContainer.visibility = View.VISIBLE
        binding.resultadosContainer.visibility = View.GONE
        binding.tvNenhumResultado.visibility = View.GONE
    }

    private fun exibirResultados(resultados: List<Hemocentro>) {
        binding.imagemContainer.visibility = View.GONE
        binding.resultadosContainer.removeAllViews()

        if (resultados.isEmpty()) {
            binding.resultadosContainer.visibility = View.GONE
            binding.tvNenhumResultado.visibility = View.VISIBLE
            return
        }

        binding.tvNenhumResultado.visibility = View.GONE
        binding.resultadosContainer.visibility = View.VISIBLE

        resultados.forEach { hemocentro ->
            val item = ItemHemocentroBinding.inflate(layoutInflater, binding.resultadosContainer, false)
            item.tvNomeUnidade.text = hemocentro.nomeUnidade
            item.tvEndereco.text = hemocentro.endereco
            item.tvTelefone.text = "Tel. ${hemocentro.telefone}"
            item.tvHorario.text = hemocentro.horarioFuncionamento
            binding.resultadosContainer.addView(item.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
