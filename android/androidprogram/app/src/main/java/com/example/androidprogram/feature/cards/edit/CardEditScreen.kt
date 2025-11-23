package com.example.androidprogram.feature.cards.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CardEditScreen(vm: CardEditViewModel, onSaved: (Long) -> Unit) {
    val s by vm.state.collectAsState()
    Scaffold { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(value = s.name, onValueChange = { vm.dispatch(CardEditIntent.NameChanged(it), onSaved) }, label = { Text("姓名") }, isError = s.name.isBlank(), supportingText = { if (s.name.isBlank()) Text("必填") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.avatarUri, onValueChange = { vm.dispatch(CardEditIntent.AvatarChanged(it), onSaved) }, label = { Text("头像URI") }, isError = s.avatarUri.isBlank(), supportingText = { if (s.avatarUri.isBlank()) Text("必填，可使用资源URI") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.position, onValueChange = { vm.dispatch(CardEditIntent.PositionChanged(it), onSaved) }, label = { Text("职位") }, isError = s.position.isBlank(), supportingText = { if (s.position.isBlank()) Text("必填") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.department, onValueChange = { vm.dispatch(CardEditIntent.DepartmentChanged(it), onSaved) }, label = { Text("部门") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.company, onValueChange = { vm.dispatch(CardEditIntent.CompanyChanged(it), onSaved) }, label = { Text("公司") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.category, onValueChange = { vm.dispatch(CardEditIntent.CategoryChanged(it), onSaved) }, label = { Text("分类") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.phone, onValueChange = { vm.dispatch(CardEditIntent.PhoneChanged(it), onSaved) }, label = { Text("手机号") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone), isError = s.phone.isNotBlank() && !s.phone.matches(Regex("^1\\d{10}$")), supportingText = { if (s.phone.isNotBlank() && !s.phone.matches(Regex("^1\\d{10}$")) ) Text("请输入11位有效手机号") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.email, onValueChange = { vm.dispatch(CardEditIntent.EmailChanged(it), onSaved) }, label = { Text("邮箱") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email), isError = s.email.isNotBlank() && !s.email.contains("@"), supportingText = { if (s.email.isNotBlank() && !s.email.contains("@")) Text("请输入有效邮箱") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.address, onValueChange = { vm.dispatch(CardEditIntent.AddressChanged(it), onSaved) }, label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = s.note, onValueChange = { vm.dispatch(CardEditIntent.NoteChanged(it), onSaved) }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { vm.dispatch(CardEditIntent.Save, onSaved) }, enabled = s.canSave && !s.saving) {
                if (s.saving) CircularProgressIndicator() else Text("保存")
            }
        }
    }
}
