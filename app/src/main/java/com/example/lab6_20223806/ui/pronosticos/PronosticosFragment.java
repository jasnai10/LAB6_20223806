package com.example.lab6_20223806.ui.pronosticos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab6_20223806.R;
import com.example.lab6_20223806.databinding.FragmentPronosticosBinding;
import com.example.lab6_20223806.model.Pronostico;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PronosticosFragment extends Fragment implements PronosticoAdapter.OnItemActionListener {

    private static final String TAG = "PronosticosFragment";
    private FragmentPronosticosBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private final List<Pronostico> pronosticos = new ArrayList<>();
    private PronosticoAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPronosticosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new PronosticoAdapter(pronosticos, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.fab.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), RegistrarPronosticoActivity.class)));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Registrar listener aquí garantiza que se recargue al volver de otra Activity o Fragment
        escucharPronosticos();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void escucharPronosticos() {
        // Sin orderBy para evitar requerir índice compuesto en Firestore;
        // el ordenamiento por fecha se hace en memoria.
        listenerRegistration = db.collection("pronosticos")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error al escuchar pronósticos: " + e.getMessage(), e);
                        return;
                    }
                    if (snapshots == null || binding == null) return;

                    pronosticos.clear();
                    for (var doc : snapshots.getDocuments()) {
                        Pronostico p = doc.toObject(Pronostico.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            pronosticos.add(p);
                        }
                    }

                    // Ordenar por fecha descendente en memoria
                    Collections.sort(pronosticos, (a, b) -> {
                        if (a.getFechaPartido() == null) return 1;
                        if (b.getFechaPartido() == null) return -1;
                        return b.getFechaPartido().compareTo(a.getFechaPartido());
                    });

                    adapter.notifyDataSetChanged();
                    binding.tvSinPronosticos.setVisibility(
                            pronosticos.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override
    public void onEditar(Pronostico pronostico) {
        Intent intent = new Intent(getActivity(), EditarPronosticoActivity.class);
        intent.putExtra("pronosticoId", pronostico.getId());
        startActivity(intent);
    }

    @Override
    public void onEliminar(Pronostico pronostico) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirmar_eliminar))
                .setMessage(getString(R.string.confirmar_eliminar_msg))
                .setPositiveButton(getString(R.string.eliminar), (dialog, which) ->
                        db.collection("pronosticos").document(pronostico.getId())
                                .delete()
                                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar", e)))
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
