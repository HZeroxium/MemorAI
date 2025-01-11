package com.example.memorai.presentation.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.memorai.domain.model.Album;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

public class AlbumViewModelTest {

    // Rule to execute LiveData tasks synchronously for testing
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Test
    public void testLoadDummyAlbums_success() {
        // Arrange
        AlbumViewModel viewModel = new AlbumViewModel();
        Observer<List<Album>> observer = Mockito.mock(Observer.class);
        viewModel.getAlbumList().observeForever(observer);

        // Act
        viewModel.loadDummyAlbums();

        // Assert
        assertNotNull(viewModel.getAlbumList().getValue());
        assertEquals(2, viewModel.getAlbumList().getValue().size()); // Should pass
        assertEquals("Album 1", viewModel.getAlbumList().getValue().get(0).getTitle()); // Should pass
    }

//    @Test
//    public void testLoadDummyAlbums_failure() {
//        // Arrange
//        AlbumViewModel viewModel = new AlbumViewModel();
//        Observer<List<Album>> observer = Mockito.mock(Observer.class);
//        viewModel.getAlbumList().observeForever(observer);
//
//        // Act
//        viewModel.loadDummyAlbums();
//
//        // Assert (Intentionally incorrect test cases)
//        assertEquals(3, viewModel.getAlbumList().getValue().size()); // Should fail
//        assertEquals("Invalid Album Title", viewModel.getAlbumList().getValue().get(0).getTitle()); // Should fail
//    }
}
