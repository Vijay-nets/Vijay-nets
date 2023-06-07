package com.example.piashop.ui.views.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.piashop.R
import com.example.piashop.ui.theme.PurchaseItemBackground

/** Displays the drop down menu to change currency */
@Composable
internal fun CurrencySelector(
    currencies: () -> List<CurrencyLocale>,
    currentSelection: CurrencyLocale,
    onSelect: (CurrencyLocale) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurchaseItemBackground)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.select_currency))
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                modifier = Modifier.clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentSelection.currencyCode,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colors.secondaryVariant
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    tint = MaterialTheme.colors.secondaryVariant,
                    contentDescription = null
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currencies().forEach { item ->
                        DropdownMenuItem(onClick = {
                            onSelect(item)
                            expanded = false
                        }) {
                            Text(
                                text = item.currencyCode,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colors.secondaryVariant
                            )
                        }
                    }
                }
            }
        }
    }
}