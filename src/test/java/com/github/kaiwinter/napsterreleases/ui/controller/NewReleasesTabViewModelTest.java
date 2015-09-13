package com.github.kaiwinter.napsterreleases.ui.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.GenreData;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import de.saxsys.javafx.test.JfxRunner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import junit.framework.Assert;
import retrofit.Callback;

@RunWith(JfxRunner.class)
public final class NewReleasesTabViewModelTest {

	private static Injector injector = Guice.createInjector(getTestModule());

	private static Module getTestModule() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				RhapsodySdkWrapper rhapsodySdkWrapperMock = mock(RhapsodySdkWrapper.class);
				Collection<GenreData> genres = Collections.singletonList(new GenreData());
				doAnswer(new LoadGenresResultAnswer(genres)).when(rhapsodySdkWrapperMock).loadGenres(any(Callback.class));

				Collection<AlbumData> releases = Collections.singletonList(new AlbumData());
				doAnswer(new LoadNewReleasesAnswer(releases)).when(rhapsodySdkWrapperMock).loadGenreNewReleases(anyString(), anyInt(),
						any(Callback.class));

				SharedViewModel sharedViewModelMock = mock(SharedViewModel.class);
				when(sharedViewModelMock.getRhapsodySdkWrapper()).thenReturn(rhapsodySdkWrapperMock);
				bind(SharedViewModel.class).toInstance(sharedViewModelMock);
			}
		};
	}

	/**
	 * Loads the genres and tests if the genre list contains the loaded genres. The genre list should contain 3 genres. One loaded from the
	 * wrapper and two synthetically created.
	 */
	@Test
	public void testLoadGenres() throws InterruptedException {
		NewReleasesTabViewModel viewModel = injector.getInstance(NewReleasesTabViewModel.class);
		viewModel.releasesProperty().set(FXCollections.observableArrayList());
		viewModel.loadGenres();

		Thread.sleep(500);// wait for callback
		TreeItem<GenreData> treeItem = viewModel.genresProperty().get();
		ObservableList<TreeItem<GenreData>> children = treeItem.getChildren();
		Assert.assertEquals(3, children.size());
	}

	/**
	 * Loads the releases of a genre and tests if the new releases list contains the loaded release.
	 */
	@Test
	public void testLoadNewReleasesOfGenre() throws InterruptedException {
		NewReleasesTabViewModel viewModel = injector.getInstance(NewReleasesTabViewModel.class);
		viewModel.releasesProperty().set(FXCollections.observableArrayList());

		GenreData genreData = new GenreData();
		viewModel.selectedGenreProperty().set(new TreeItem<GenreData>(genreData));
		viewModel.showNewReleases(genreData);

		Thread.sleep(500); // wait for callback
		Assert.assertEquals(1, viewModel.releasesProperty().size());
	}

	private static class LoadGenresResultAnswer implements Answer<Void> {
		private Collection<GenreData> genres;

		public LoadGenresResultAnswer(Collection<GenreData> genres) {
			this.genres = genres;
		}

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			((Callback<Collection<GenreData>>) invocation.getArguments()[0]).success(genres, null);
			return null;
		}
	}

	private static class LoadNewReleasesAnswer implements Answer<Void> {
		private Collection<AlbumData> releases;

		public LoadNewReleasesAnswer(Collection<AlbumData> releases) {
			this.releases = releases;
		}

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			((Callback<Collection<AlbumData>>) invocation.getArguments()[2]).success(releases, null);
			return null;
		}
	}
}
