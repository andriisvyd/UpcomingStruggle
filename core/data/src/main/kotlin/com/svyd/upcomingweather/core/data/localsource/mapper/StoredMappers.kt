package com.svyd.upcomingweather.core.data.localsource.mapper

import com.svyd.upcomingweather.core.data.localsource.dto.StoredLabel
import com.svyd.upcomingweather.core.data.localsource.dto.StoredPlace
import com.svyd.upcomingweather.core.data.localsource.dto.StoredSelection
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace

internal fun PlaceLabel.toStored(): StoredLabel = when (this) {
    is PlaceLabel.Named -> StoredLabel(name = name)
    PlaceLabel.NamelessCurrentLocation -> StoredLabel(name = null)
}

internal fun StoredLabel.toLabel(): PlaceLabel =
    name?.let(PlaceLabel::Named) ?: PlaceLabel.NamelessCurrentLocation

internal fun SelectedPlace.toStored(): StoredSelection = StoredSelection(
    label = label.toStored(),
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
)

internal fun StoredSelection.toSelectedPlace(): SelectedPlace = SelectedPlace(
    label = label.toLabel(),
    coordinates = Coordinates(latitude = latitude, longitude = longitude),
)

internal fun Place.toStored(): StoredPlace = StoredPlace(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = coordinates.latitude,
    longitude = coordinates.longitude,
)

internal fun StoredPlace.toPlace(): Place = Place(
    id = id,
    name = name,
    region = region,
    country = country,
    coordinates = Coordinates(latitude = latitude, longitude = longitude),
)
