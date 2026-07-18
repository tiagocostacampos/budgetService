package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Budget
import com.example.data.model.Client
import com.example.data.model.SelectedService
import com.example.data.model.Service
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    
    // Auth State
    var isAuthenticated by mutableStateOf(false)
        private set
    var isPinConfigured by mutableStateOf(false)
        private set
    var loginErrorMessage by mutableStateOf("")
        private set
    var loggedInUserEmail by mutableStateOf("")
        private set
    var loggedInUserName by mutableStateOf("")
        private set

    // Database Flows
    val clients: StateFlow<List<Client>>
    val services: StateFlow<List<Service>>
    val budgets: StateFlow<List<Budget>>

    // Share Preferences for PIN authentication
    private val prefs = application.getSharedPreferences("orcafacil_prefs", Context.MODE_PRIVATE)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BudgetRepository(db.clientDao(), db.serviceDao(), db.budgetDao())

        clients = repository.allClients.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        services = repository.allServices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgets = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Check if PIN is already set
        isPinConfigured = prefs.contains("app_pin")
        
        // Check session
        isAuthenticated = prefs.getBoolean("is_authenticated", false)
        loggedInUserEmail = prefs.getString("logged_in_email", "") ?: ""
        loggedInUserName = prefs.getString("logged_in_name", "") ?: ""
        
        // Populate sample data if DB is completely empty (first run) to give the user an amazing immediate dashboard experience!
        viewModelScope.launch {
            // Check if databases are empty, if so, seed sample data
            repository.allClients.collect { list ->
                if (list.isEmpty()) {
                    seedSampleData()
                }
            }
        }
    }

    private suspend fun seedSampleData() {
        val c1 = Client(name = "Ana Silva", phone = "11988887777", email = "ana.silva@email.com", address = "Av. Paulista, 1000 - SP")
        val c2 = Client(name = "Bruno Costa", phone = "21977776666", email = "bruno.c@email.com", address = "Rua Copacabana, 500 - RJ")
        val c3 = Client(name = "Clara Oliveira", phone = "31966665555", email = "clara.o@email.com", address = "Rua da Bahia, 300 - BH")
        
        val c1Id = repository.insertClient(c1)
        val c2Id = repository.insertClient(c2)
        val c3Id = repository.insertClient(c3)

        val s1 = Service(name = "Desenvolvimento de Software", description = "Desenvolvimento de aplicativo móvel sob medida", price = 4500.0)
        val s2 = Service(name = "Consultoria de Design UX/UI", description = "Protótipo de alta fidelidade e design de telas", price = 2200.0)
        val s3 = Service(name = "Hospedagem & Nuvem", description = "Configuração de banco de dados e deploy na AWS/Firebase", price = 850.0)
        val s4 = Service(name = "Manutenção Mensal", description = "Suporte, correção de bugs e melhorias contínuas", price = 1200.0)

        val s1Id = repository.insertService(s1)
        val s2Id = repository.insertService(s2)
        val s3Id = repository.insertService(s3)
        val s4Id = repository.insertService(s4)

        // Add some budgets
        val sel1 = SelectedService(s1Id, s1.name, s1.description, s1.price, 1)
        val sel2 = SelectedService(s2Id, s2.name, s2.description, s2.price, 1)
        val sel3 = SelectedService(s3Id, s3.name, s3.description, s3.price, 2)
        val sel4 = SelectedService(s4Id, s4.name, s4.description, s4.price, 1)
        
        val arr1 = JSONArray().apply {
            put(sel1.toJsonObject())
            put(sel3.toJsonObject())
        }
        val b1 = Budget(
            clientId = c1Id,
            clientName = c1.name,
            clientPhone = c1.phone,
            clientEmail = c1.email,
            clientAddress = c1.address,
            dateMillis = System.currentTimeMillis() - 45 * 24 * 60 * 60 * 1000L, // 45 days ago
            totalAmount = 6200.0,
            discount = 100.0,
            status = "Concluído",
            servicesJson = arr1.toString()
        )
        repository.insertBudget(b1)

        val arr2 = JSONArray().apply {
            put(sel2.toJsonObject())
            put(sel3.toJsonObject())
        }
        val b2 = Budget(
            clientId = c2Id,
            clientName = c2.name,
            clientPhone = c2.phone,
            clientEmail = c2.email,
            clientAddress = c2.address,
            dateMillis = System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L, // 15 days ago
            totalAmount = 3050.0,
            status = "Aprovado",
            servicesJson = arr2.toString()
        )
        repository.insertBudget(b2)

        val arr3 = JSONArray().apply {
            put(sel4.toJsonObject())
        }
        val b3 = Budget(
            clientId = c3Id,
            clientName = c3.name,
            clientPhone = c3.phone,
            clientEmail = c3.email,
            clientAddress = c3.address,
            dateMillis = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L, // 5 days ago
            totalAmount = 1200.0,
            status = "Pendente",
            servicesJson = arr3.toString()
        )
        repository.insertBudget(b3)
    }

    // PIN Authentication logic
    fun registerPin(pin: String): Boolean {
        if (pin.length < 4) {
            loginErrorMessage = "O PIN deve ter pelo menos 4 dígitos."
            return false
        }
        prefs.edit().putString("app_pin", pin).apply()
        isPinConfigured = true
        isAuthenticated = true
        loginErrorMessage = ""
        return true
    }

    fun authenticate(pin: String): Boolean {
        val savedPin = prefs.getString("app_pin", null)
        return if (savedPin == pin) {
            isAuthenticated = true
            loginErrorMessage = ""
            true
        } else {
            loginErrorMessage = "PIN incorreto. Tente novamente."
            false
        }
    }

    fun logout() {
        isAuthenticated = false
        loggedInUserEmail = ""
        loggedInUserName = ""
        prefs.edit()
            .putBoolean("is_authenticated", false)
            .remove("logged_in_email")
            .remove("logged_in_name")
            .apply()
    }

    fun loginWithEmail(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            loginErrorMessage = "E-mail e senha são obrigatórios."
            return false
        }
        
        val lowerEmail = email.trim().lowercase()
        val storedUserData = prefs.getString("user_credentials_$lowerEmail", null)
        
        val passwordAndName = if (storedUserData != null) {
            val parts = storedUserData.split("|")
            if (parts.size >= 2) parts[0] to parts[1] else null
        } else if (lowerEmail == "admin@email.com" || lowerEmail == "admin@gmail.com") {
            "admin123" to "Administrador"
        } else if (lowerEmail == "tiagocostac@gmail.com") {
            "senha123" to "Tiago Costa"
        } else {
            null
        }

        if (passwordAndName != null && passwordAndName.first == password) {
            isAuthenticated = true
            loggedInUserEmail = lowerEmail
            loggedInUserName = passwordAndName.second
            loginErrorMessage = ""
            
            prefs.edit()
                .putBoolean("is_authenticated", true)
                .putString("logged_in_email", lowerEmail)
                .putString("logged_in_name", passwordAndName.second)
                .apply()
            return true
        } else {
            loginErrorMessage = "E-mail ou senha incorretos."
            return false
        }
    }

    fun registerWithEmail(name: String, email: String, password: String): Boolean {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            loginErrorMessage = "Todos os campos são obrigatórios."
            return false
        }
        if (!email.contains("@")) {
            loginErrorMessage = "Por favor, insira um e-mail válido."
            return false
        }
        if (password.length < 6) {
            loginErrorMessage = "A senha deve ter no mínimo 6 caracteres."
            return false
        }

        val lowerEmail = email.trim().lowercase()
        if (prefs.contains("user_credentials_$lowerEmail") || lowerEmail == "admin@email.com" || lowerEmail == "tiagocostac@gmail.com") {
            loginErrorMessage = "Este e-mail já está cadastrado."
            return false
        }

        // Store credentials
        prefs.edit().putString("user_credentials_$lowerEmail", "$password|$name").apply()
        
        // Log in immediately
        isAuthenticated = true
        loggedInUserEmail = lowerEmail
        loggedInUserName = name
        loginErrorMessage = ""
        
        prefs.edit()
            .putBoolean("is_authenticated", true)
            .putString("logged_in_email", lowerEmail)
            .putString("logged_in_name", name)
            .apply()
        
        return true
    }

    fun loginOrRegisterWithGoogle(email: String, name: String) {
        val lowerEmail = email.trim().lowercase()
        isAuthenticated = true
        loggedInUserEmail = lowerEmail
        loggedInUserName = name
        loginErrorMessage = ""
        
        // Save in stored credentials if not exist (using a placeholder password)
        if (!prefs.contains("user_credentials_$lowerEmail")) {
            prefs.edit().putString("user_credentials_$lowerEmail", "google_oauth_bypass|$name").apply()
        }
        
        prefs.edit()
            .putBoolean("is_authenticated", true)
            .putString("logged_in_email", lowerEmail)
            .putString("logged_in_name", name)
            .apply()
    }

    // Client CRUD Operations
    fun addClient(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            repository.insertClient(Client(name = name, phone = phone, email = email, address = address))
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    // Service CRUD Operations
    fun addService(name: String, description: String, price: Double) {
        viewModelScope.launch {
            repository.insertService(Service(name = name, description = description, price = price))
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    // Budget Operations
    fun createBudget(
        client: Client,
        selectedServices: List<SelectedService>,
        discount: Double,
        addition: Double,
        totalAmount: Double,
        status: String = "Pendente",
        onComplete: (Budget) -> Unit
    ) {
        viewModelScope.launch {
            val jsonArr = JSONArray()
            selectedServices.forEach { jsonArr.put(it.toJsonObject()) }
            
            val budget = Budget(
                clientId = client.id,
                clientName = client.name,
                clientPhone = client.phone,
                clientEmail = client.email,
                clientAddress = client.address,
                discount = discount,
                addition = addition,
                totalAmount = totalAmount,
                status = status,
                servicesJson = jsonArr.toString()
            )
            val budgetId = repository.insertBudget(budget)
            val savedBudget = budget.copy(id = budgetId)
            onComplete(savedBudget)
        }
    }

    fun updateBudgetStatus(budget: Budget, newStatus: String) {
        viewModelScope.launch {
            repository.updateBudget(budget.copy(status = newStatus))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }
}
