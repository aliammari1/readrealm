import 'package:flutter/material.dart';

const apiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'http://localhost:3000',
);

// ReadRealm enchanted library palette — mirrors the Android app.
const deepLibraryBrown = Color(0xFF1A0F0A);
const richMahogany = Color(0xFF4A2C2A);
const warmLeather = Color(0xFF8B5A2B);
const gildedGold = Color(0xFFD4AF37);
const ancientParchment = Color(0xFFF5E6C8);
const candlelightGlow = Color(0xFFFFE4B5);
const mysticPurple = Color(0xFF2D1B4E);

const primaryColor = gildedGold;
const secondaryColor = richMahogany;
const bgColor = deepLibraryBrown;

const defaultPadding = 16.0;
