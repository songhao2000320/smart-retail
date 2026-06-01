import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const currentStoreId = ref(null)
  const currentStoreName = ref('')
  const sidebarCollapsed = ref(false)

  function setCurrentStore(storeId, storeName) {
    currentStoreId.value = storeId
    currentStoreName.value = storeName || ''
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    currentStoreId,
    currentStoreName,
    sidebarCollapsed,
    setCurrentStore,
    toggleSidebar
  }
})
