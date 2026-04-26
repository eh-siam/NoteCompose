package com.example.notecompose.presentation.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notecompose.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteItem(
    note: Note,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onLockClick: () -> Unit = {},
    onFinishedClick: () -> Unit = {}
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val categoryColor = when (note.category) {
        "Work" -> Color(0xFF90CAF9)
        "Birthday" -> Color(0xFFF48FB1)
        "Important" -> Color(0xFFFFAB91)
        "Occasions" -> Color(0xFFA5D6A7)
        "Complete" -> Color(0xFF81C784)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Indicator Strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(categoryColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = sdf.format(Date(note.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = categoryColor
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.pin != null) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp).padding(end = 4.dp)
                    )
                }

                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (note.isBookmarked) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                isMenuExpanded = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note.pin == null) "Lock" else "Unlock/Change PIN") },
                            onClick = {
                                isMenuExpanded = false
                                onLockClick()
                            },
                            leadingIcon = { 
                                Icon(
                                    imageVector = if (note.pin == null) Icons.Default.LockOpen else Icons.Default.Lock, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(18.dp)
                                ) 
                            }
                        )
                        if (note.category == "Work") {
                            DropdownMenuItem(
                                text = { Text("Finished") },
                                onClick = {
                                    isMenuExpanded = false
                                    onFinishedClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                isMenuExpanded = false
                                onDeleteClick()
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                ) 
                            }
                        )
                    }
                }
            }
        }
    }
}
