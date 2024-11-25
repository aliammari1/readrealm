package tn.esprit.libraryapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

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
                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp) // Shape for the border
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable { }
            .padding(horizontal = 35.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (tint != null) {
            Icon(
                painter = painterResource(image),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(30.dp)
            )
        } else {
            Image(
                painter = painterResource(image),
                contentDescription = contentDescription,
                modifier = Modifier.size(30.dp)
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
    onTrailingClick: () -> Unit = {}
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
            onTrailingClick = onTrailingClick
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
            onTrailingClick = onTrailingClick
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
    onTrailingClick: () -> Unit = {}
) {
    OutlinedTextField(
        value = textFieldState,
        onValueChange = onTextChange,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onBackground
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5F),
                    modifier = Modifier.clickable { onLeadingClick() }
                )
            }
        },
        trailingIcon = {
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5F),
                    modifier = Modifier.clickable { onTrailingClick() }
                )
            } else if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onTrailingClick() }
                )
            }
        },
        placeholder = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
            )
        },
        modifier = modifier
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
    onTrailingClick: () -> Unit = {}
) {
    OutlinedTextField(
        value = textFieldState,
        onValueChange = onTextChange,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onBackground
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5F),
                    modifier = Modifier.clickable { onLeadingClick() }
                )
            }
        },
        trailingIcon = {
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5F),
                    modifier = Modifier.clickable { onTrailingClick() }
                )
            } else if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onTrailingClick() }
                )
            }
        },
        placeholder = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
            )
        },
        modifier = modifier
    )
}

