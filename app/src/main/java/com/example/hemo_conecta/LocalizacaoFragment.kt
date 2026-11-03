package com.example.hemo_conecta

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hemo_conecta.data.Hemocentro
import com.example.hemo_conecta.data.HemocentroProximo
import com.example.hemo_conecta.data.HemocentroRepository
import com.example.hemo_conecta.databinding.FragmentLocalizacaoBinding
import com.example.hemo_conecta.databinding.ItemHemocentroBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

/**
 * Tela de localização de hemocentros.
 *
 * - Ao abrir, pede permissão de localização e mostra automaticamente o
 *   hemocentro mais próximo do usuário.
 * - Digitar na barra de pesquisa busca em /hemocentros no Firebase (via
 *   [HemocentroRepository]) e mostra os resultados como cards.
 */
class LocalizacaoFragment : Fragment() {

    private var _binding: FragmentLocalizacaoBinding? = null
    private val binding get() = _binding!!

    // true quando o card do hemocentro mais próximo já está na tela
    private var maisProximoCarregado = false
    // true enquanto uma busca de localização está em andamento
    private var buscandoLocalizacao = false

    private val permissaoLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedida = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedida) {
            obterLocalizacao()
        } else {
            mostrarStatusMaisProximo("Permita o acesso à localização para ver o hemocentro mais próximo de você. Toque aqui para tentar de novo.")
        }
    }

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

        // a view foi (re)criada: começa do zero
        maisProximoCarregado = false
        buscandoLocalizacao = false

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                buscar(s?.toString().orEmpty())
            }
        })

        // Tocar na mensagem de status tenta localizar de novo
        binding.tvMaisProximoStatus.setOnClickListener {
            if (!buscandoLocalizacao) verificarPermissaoELocalizar()
        }

        verificarPermissaoELocalizar()
    }

    override fun onResume() {
        super.onResume()
        // Ao voltar das configurações (depois de ligar o GPS ou liberar a
        // permissão), tenta de novo sozinho, sem precisar reabrir a tela.
        if (!maisProximoCarregado && !buscandoLocalizacao && temPermissao()) {
            obterLocalizacao()
        }
    }

    // ---------------------------------------------------------------
    // Hemocentro mais próximo (localização do usuário)
    // ---------------------------------------------------------------

    private fun temPermissao(): Boolean {
        val contexto = requireContext()
        return ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun verificarPermissaoELocalizar() {
        if (temPermissao()) {
            obterLocalizacao()
        } else {
            mostrarStatusMaisProximo("Aguardando permissão de localização...")
            permissaoLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission") // a permissão já foi verificada/pedida acima
    private fun obterLocalizacao() {
        buscandoLocalizacao = true
        mostrarStatusMaisProximo("Buscando o hemocentro mais próximo...")

        val cliente = LocationServices.getFusedLocationProviderClient(requireActivity())
        cliente.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { localizacao ->
            if (_binding == null) return@addOnSuccessListener
            if (localizacao != null) {
                buscarMaisProximo(localizacao)
            } else {
                // Sem posição "fresca": tenta a última posição conhecida do aparelho
                cliente.lastLocation.addOnSuccessListener { ultima ->
                    if (_binding == null) return@addOnSuccessListener
                    if (ultima != null) {
                        buscarMaisProximo(ultima)
                    } else {
                        falhaLocalizacao("Não foi possível obter sua localização. Verifique se a localização do celular está ligada. Toque aqui para tentar de novo.")
                    }
                }.addOnFailureListener {
                    if (_binding == null) return@addOnFailureListener
                    falhaLocalizacao("Não foi possível obter sua localização. Toque aqui para tentar de novo.")
                }
            }
        }.addOnFailureListener {
            if (_binding == null) return@addOnFailureListener
            falhaLocalizacao("Não foi possível obter sua localização. Toque aqui para tentar de novo.")
        }
    }

    private fun falhaLocalizacao(mensagem: String) {
        buscandoLocalizacao = false
        mostrarStatusMaisProximo(mensagem)
    }

    private fun buscarMaisProximo(localizacao: Location) {
        HemocentroRepository.buscarMaisProximo(
            requireContext().applicationContext,
            localizacao.latitude,
            localizacao.longitude
        ) { maisProximo ->
            buscandoLocalizacao = false
            if (_binding == null) return@buscarMaisProximo
            exibirMaisProximo(maisProximo)
        }
    }

    private fun exibirMaisProximo(maisProximo: HemocentroProximo?) {
        binding.cardMaisProximoContainer.removeAllViews()

        if (maisProximo == null) {
            mostrarStatusMaisProximo("Não foi possível calcular o hemocentro mais próximo agora. Toque aqui para tentar de novo.")
            return
        }

        maisProximoCarregado = true
        binding.tvMaisProximoStatus.visibility = View.GONE
        val item = ItemHemocentroBinding.inflate(layoutInflater, binding.cardMaisProximoContainer, false)
        preencherCard(item, maisProximo.hemocentro)
        item.tvDistancia.text =
            String.format(Locale("pt", "BR"), "A %.1f km de você", maisProximo.distanciaKm)
        item.tvDistancia.visibility = View.VISIBLE
        binding.cardMaisProximoContainer.addView(item.root)
    }

    private fun mostrarStatusMaisProximo(mensagem: String) {
        binding.cardMaisProximoContainer.removeAllViews()
        binding.tvMaisProximoStatus.text = mensagem
        binding.tvMaisProximoStatus.visibility = View.VISIBLE
    }

    // ---------------------------------------------------------------
    // Busca por texto
    // ---------------------------------------------------------------

    private fun buscar(termo: String) {
        if (termo.isBlank()) {
            mostrarImagemPadrao()
            return
        }

        binding.maisProximoContainer.visibility = View.GONE

        HemocentroRepository.buscarPorCidade(termo) { resultados ->
            // o callback do Firebase pode voltar depois da view já ter sido destruída
            if (_binding == null) return@buscarPorCidade
            exibirResultados(resultados)
        }
    }

    private fun mostrarImagemPadrao() {
        binding.maisProximoContainer.visibility = View.VISIBLE
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
            preencherCard(item, hemocentro)
            binding.resultadosContainer.addView(item.root)
        }
    }

    private fun preencherCard(item: ItemHemocentroBinding, hemocentro: Hemocentro) {
        item.tvNomeUnidade.text = hemocentro.nomeUnidade
        item.tvEndereco.text = hemocentro.endereco
        item.tvTelefone.text = "Tel. ${hemocentro.telefone}"
        item.tvHorario.text = hemocentro.horarioFuncionamento
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
