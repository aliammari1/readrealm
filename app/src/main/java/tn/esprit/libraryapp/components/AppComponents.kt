package tn.esprit.libraryapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// Library theme colors
private val LeatherBrown = Color(0xFF8B4513)
private val AntiqueGold = Color(0xFFDAA520)
private val Parchment = Color(0xFFF5DEB3)
private val DarkMahogany = Color(0xFF3C1810)
private val WarmCream = Color(0xFFFFF8DC)

@Composable
fun AuthOption(
    modifier: Modifier = Modifier,
    image: Int,
    tint: Color? = null,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .border(
                BorderStroke(2.dp, Brush.linearGradient(
                    colors = listOf(
                        AntiqueGold.copy(alpha = 0.5f),
                        LeatherBrown.copy(alpha = 0.3f),
                    )
                )),
                shape = RoundedCornerShape(14.dp),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(WarmCream.copy(alpha = 0.9f))
            .clickable { }
            .padding(horizontal = 35.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (tint != null) {
            Icon(
                painter = painterResource(image),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(30.dp),
            )
        } else {
            Image(
                painter = painterResource(image),
                contentDescription = contentDescription,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyTextField(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingText: String? = null,
    textFieldState: String,
    onTextChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    onLeadingClick: () -> Unit = {},
    onTrailingClick: () -> Unit = {},
) {
    if (isPassword) {
        PasswordTextField(
            modifier = modifier,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            trailingText = trailingText,
            textFieldState = textFieldState,
            onTextChange = onTextChange,
            hint = hint,
            onLeadingClick = onLeadingClick,
            onTrailingClick = onTrailingClick,
        )
    } else {
        TextTextField(
            modifier = modifier,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            trailingText = trailingText,
            textFieldState = textFieldState,
            onTextChange = onTextChange,
            hint = hint,
            keyboardType = keyboardType,
            onLeadingClick = onLeadingClick,
            onTrailingClick = onTrailingClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextTextField(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingText: String? = null,
    textFieldState: String,
    onTextChange: (String) -> Unit,
    hint: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onLeadingClick: () -> Unit = {},
    onTrailingClick: () -> Unit = {},
) {
    OutlinedTextField(
        value = textFieldState,
        onValueChange = onTextChange,
        textStyle = LocalTextStyle.current.copy(
            color = DarkMahogany,
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LeatherBrown,
            unfocusedBorderColor = LeatherBrown.copy(alpha = 0.4f),
            focusedContainerColor = WarmCream.copy(alpha = 0.5f),
            unfocusedContainerColor = WarmCream.copy(alpha = 0.3f),
            cursorColor = LeatherBrown,
            focusedLeadingIconColor = LeatherBrown,
            unfocusedLeadingIconColor = LeatherBrown.copy(alpha = 0.6f),
            focusedTrailingIconColor = AntiqueGold,
            unfocusedTrailingIconColor = AntiqueGold.copy(alpha = 0.6f),
        ),
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.clickable { onLeadingClick() },
                )
            }
        },
        trailingIcon = {
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.clickable { onTrailingClick() },
                )
            } else if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = LeatherBrown,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onTrailingClick() },
                )
            }
        },
        placeholder = {
            Text(
                text = hint,
                color = LeatherBrown.copy(alpha = 0.5f),
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingText: String? = null,
    textFieldState: String,
    onTextChange: (String) -> Unit,
    hint: String,
    onLeadingClick: () -> Unit = {},
    onTrailingClick: () -> Unit = {},
) {
    OutlinedTextField(
        value = textFieldState,
        onValueChange = onTextChange,
        textStyle = LocalTextStyle.current.copy(
            color = DarkMahogany,
            fontWeight = FontWeight.Medium,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LeatherBrown,
            unfocusedBorderColor = LeatherBrown.copy(alpha = 0.4f),
            focusedContainerColor = WarmCream.copy(alpha = 0.5f),
            unfocusedContainerColor = WarmCream.copy(alpha = 0.3f),
            cursorColor = LeatherBrown,
            focusedLeadingIconColor = LeatherBrown,
            unfocusedLeadingIconColor = LeatherBrown.copy(alpha = 0.6f),
            focusedTrailingIconColor = AntiqueGold,
            unfocusedTrailingIconColor = AntiqueGold.copy(alpha = 0.6f),
        ),
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.clickable { onLeadingClick() },
                )
            }
        },
        trailingIcon = {
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.clickable { onTrailingClick() },
                )
            } else if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = LeatherBrown,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onTrailingClick() },
                )
            }
        },
        placeholder = {
            Text(
                text = hint,
                color = LeatherBrown.copy(alpha = 0.5f),
            )
        },
        modifier = modifier,
    )
}
