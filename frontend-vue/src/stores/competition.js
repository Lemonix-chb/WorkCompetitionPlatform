import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get } from '@/utils/api'
import { showError } from '@/utils/messageUtils'

export const useCompetitionStore = defineStore('competition', () => {
  const competitions = ref([])
  const currentCompetition = ref(null)
  const tracks = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 获取赛事列表（从后端）
  const fetchCompetitions = async () => {
    loading.value = true
    error.value = null
    try {
      const data = await get('/competitions')
      competitions.value = data.records || []
    } catch (e) {
      error.value = '获取赛事列表失败'
      showError('获取赛事列表失败')
      console.error('Fetch competitions error:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取赛事详情（从后端）
  const fetchCompetitionById = async (id) => {
    loading.value = true
    error.value = null
    try {
      const data = await get(`/competitions/${id}`)
      currentCompetition.value = data
    } catch (e) {
      error.value = '获取赛事详情失败'
      showError('获取赛事详情失败')
      console.error('Fetch competition error:', e)
    } finally {
      loading.value = false
    }
  }

  // 获取赛道列表（从后端）
  const fetchTracks = async (competitionId) => {
    loading.value = true
    error.value = null
    try {
      const data = await get(`/competitions/${competitionId}/tracks`)
      tracks.value = data || []
    } catch (e) {
      error.value = '获取赛道信息失败'
      showError('获取赛道信息失败')
      console.error('Fetch tracks error:', e)
    } finally {
      loading.value = false
    }
  }

  return {
    competitions,
    currentCompetition,
    tracks,
    loading,
    error,
    fetchCompetitions,
    fetchCompetitionById,
    fetchTracks
  }
})