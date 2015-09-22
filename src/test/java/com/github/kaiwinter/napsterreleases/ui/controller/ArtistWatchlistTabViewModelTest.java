package com.github.kaiwinter.napsterreleases.ui.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.kaiwinter.napsterreleases.persistence.WatchedArtistsStore;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist;
import com.github.kaiwinter.napsterreleases.ui.model.WatchedArtist.LastRelease;
import com.github.kaiwinter.rhapsody.api.RhapsodySdkWrapper;
import com.github.kaiwinter.rhapsody.model.AlbumData;
import com.github.kaiwinter.rhapsody.model.AlbumData.Artist;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Tests for the {@link ArtistWatchlistTabViewModel}.
 */
public final class ArtistWatchlistTabViewModelTest {

	private static Injector injector = Guice.createInjector(getTestModule());

	private static WatchedArtist watchedArtistChanged;
	private static WatchedArtist watchedArtistUnchanged;

	private static Module getTestModule() {
		return new AbstractModule() {
			private WatchedArtistsStore watchedArtistsStore;

			@Override
			protected void configure() {
				RhapsodySdkWrapper rhapsodySdkWrapperMock = mock(RhapsodySdkWrapper.class);
				Collection<AlbumData> albumDataChanged = createChangedAlbum();
				when(rhapsodySdkWrapperMock.getArtistNewReleases(eq("changed artist"), anyInt())).thenReturn(albumDataChanged);

				Collection<AlbumData> albumDataUnchanged = createInitialAlbum();
				when(rhapsodySdkWrapperMock.getArtistNewReleases(eq("unchanged artist"), anyInt())).thenReturn(albumDataUnchanged);

				SharedViewModel sharedViewModelMock = mock(SharedViewModel.class);
				when(sharedViewModelMock.getRhapsodySdkWrapper()).thenReturn(rhapsodySdkWrapperMock);
				bind(SharedViewModel.class).toInstance(sharedViewModelMock);

				Set<WatchedArtist> watchedArtists = initTestWatchedArtist();

				watchedArtistsStore = mock(WatchedArtistsStore.class);
				when(watchedArtistsStore.loadWatchedArtists()).thenReturn(watchedArtists);
				bind(WatchedArtistsStore.class).toInstance(watchedArtistsStore);
			}
		};
	}

	private static Collection<AlbumData> createChangedAlbum() {
		AlbumData albumDataChanged = new AlbumData();
		albumDataChanged.name = "new album";
		albumDataChanged.released = System.currentTimeMillis();
		Collection<AlbumData> latestReleaseChanged = Collections.singletonList(albumDataChanged);
		return latestReleaseChanged;
	}

	private static Collection<AlbumData> createInitialAlbum() {
		AlbumData albumDataUnchanged = new AlbumData();
		albumDataUnchanged.name = "old album";
		Collection<AlbumData> latestReleaseUnchanged = Collections.singletonList(albumDataUnchanged);
		return latestReleaseUnchanged;
	}

	private static HashSet<WatchedArtist> initTestWatchedArtist() {
		Artist artistChanged = new Artist();
		artistChanged.id = "changed artist";
		watchedArtistChanged = new WatchedArtist(artistChanged);

		Artist artistUnchanged = new Artist();
		artistUnchanged.id = "unchanged artist";
		watchedArtistUnchanged = new WatchedArtist(artistUnchanged);

		HashSet<WatchedArtist> watchedArtists = new HashSet<>();
		watchedArtists.add(watchedArtistChanged);
		watchedArtists.add(watchedArtistUnchanged);
		return watchedArtists;
	}

	@Before
	public void initTestdata() {
		LastRelease lastRelease = new LastRelease();
		lastRelease.setAlbumName("old album");
		watchedArtistChanged.setLastRelease(lastRelease);

		LastRelease lastRelease2 = new LastRelease();
		lastRelease2.setAlbumName("old album");
		watchedArtistUnchanged.setLastRelease(lastRelease2);
	}

	@Test
	public void testChanged() {
		assertEquals("old album", watchedArtistChanged.getLastRelease().getAlbumName());

		ArtistWatchlistTabViewModel viewModel = injector.getInstance(ArtistWatchlistTabViewModel.class);
		viewModel.checkForNewReleases();

		assertEquals("new album", watchedArtistChanged.getLastRelease().getAlbumName());
		assertEquals(true, watchedArtistChanged.getLastRelease().isUpdated());
	}

	@Test
	public void testUnchanged() {
		assertEquals("old album", watchedArtistUnchanged.getLastRelease().getAlbumName());

		ArtistWatchlistTabViewModel viewModel = injector.getInstance(ArtistWatchlistTabViewModel.class);
		viewModel.checkForNewReleases();

		assertEquals("old album", watchedArtistUnchanged.getLastRelease().getAlbumName());
		assertEquals(false, watchedArtistUnchanged.getLastRelease().isUpdated());
	}
}
