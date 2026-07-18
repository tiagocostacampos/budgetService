package com.example.data.repository

import com.example.data.dao.BudgetDao
import com.example.data.dao.ClientDao
import com.example.data.dao.ServiceDao
import com.example.data.model.Budget
import com.example.data.model.Client
import com.example.data.model.Service
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val clientDao: ClientDao,
    private val serviceDao: ServiceDao,
    private val budgetDao: BudgetDao
) {
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    val allServices: Flow<List<Service>> = serviceDao.getAllServices()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun getClientById(id: Long): Client? = clientDao.getClientById(id)
    suspend fun insertClient(client: Client): Long = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)
    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)

    suspend fun getServiceById(id: Long): Service? = serviceDao.getServiceById(id)
    suspend fun insertService(service: Service): Long = serviceDao.insertService(service)
    suspend fun updateService(service: Service) = serviceDao.updateService(service)
    suspend fun deleteService(service: Service) = serviceDao.deleteService(service)

    suspend fun getBudgetById(id: Long): Budget? = budgetDao.getBudgetById(id)
    suspend fun insertBudget(budget: Budget): Long = budgetDao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)
}
