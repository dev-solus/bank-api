package com.example.foyerrouamnissi;

import com.example.foyerrouamnissi.DAO.Entities.Etudiant;
import com.example.foyerrouamnissi.DAO.Repositories.EtudiantRepository;
import com.example.foyerrouamnissi.Services.Etudiant.EtudiantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EtudiantServiceMockTest {

    @Mock
    private EtudiantRepository etudiantRepository;

    @InjectMocks
    private EtudiantService etudiantService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testAddEtudiant() {

        Etudiant sampleEtudiant = Etudiant.builder()
                .nomEt("John")
                .prenomEt("Doe")
                .build();
        when(etudiantRepository.save(any(Etudiant.class))).thenReturn(sampleEtudiant);


        Etudiant savedEtudiant = etudiantService.addEtudiant(sampleEtudiant);

        assertEquals("John", savedEtudiant.getNomEt());
        assertEquals("Doe", savedEtudiant.getPrenomEt());
    }

    @Test
    void testGetAllEtudiants() {

        List<Etudiant> etudiants = new ArrayList<>();
        etudiants.add(new Etudiant());
        when(etudiantRepository.findAll()).thenReturn(etudiants);


        List<Etudiant> result = etudiantService.getAllEtudiants();


        assertEquals(1, result.size());
    }

    @Test
    void testUpdateEtudiant() {
        // Given
        Integer existingEtudiantId = 1;
        Etudiant existingEtudiant = Etudiant.builder()
                .idEtudiant(existingEtudiantId)
                .build();
        when(etudiantRepository.existsById(existingEtudiantId)).thenReturn(true);
        when(etudiantRepository.save(any(Etudiant.class))).thenReturn(existingEtudiant);

        // When
        Etudiant updatedEtudiant = etudiantService.updateEtudiant(existingEtudiant);

        // Then
        assertEquals(existingEtudiantId, updatedEtudiant.getIdEtudiant());
    }

    @Test
    void testDeleteEtudiant() {

        Integer etudiantIdToDelete = 1;
        doNothing().when(etudiantRepository).deleteById(etudiantIdToDelete);


        etudiantService.deleteEtudiant(etudiantIdToDelete);


        verify(etudiantRepository, times(1)).deleteById(etudiantIdToDelete);
    }

    /*  @Test
    void testAddEtudiant() {
        Etudiant sampleEtudiant = Etudiant.builder()
                .nomEt("John")
                .prenomEt("Doe")
                .build();

        Etudiant savedEtudiant = etudiantService.addEtudiant(sampleEtudiant);

        Assertions.assertNotNull(savedEtudiant);
        Assertions.assertEquals("John", savedEtudiant.getNomEt());
        Assertions.assertEquals("Doe", savedEtudiant.getPrenomEt());
        // Add assertions for other properties
    }

    @Test
    void testGetAllEtudiants() {

        List<Etudiant> existingEtudiants = etudiantRepository.findAll();
        List<Etudiant> result = etudiantService.getAllEtudiants();
        Assertions.assertEquals(existingEtudiants.size(), result.size());

    }*/

}
