import type { SubmissionResponse, VehicleSubmissionRequest, SubmissionStatus } from '~/types/api'

interface ApiResponse<T> {
  data: T
  message?: string
}

/**
 * Composable for managing vehicle submissions.
 * Uses Nuxt server routes that handle authentication via BFF pattern.
 */
export function useSubmissions() {
  const submissions = ref<SubmissionResponse[]>([])
  const adminSubmissions = ref<SubmissionResponse[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  /**
   * Fetch submissions for the current user.
   */
  async function fetchMySubmissions() {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SubmissionResponse[]>>('/api/submissions/my')
      submissions.value = response.data
      return submissions.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to fetch submissions'
      submissions.value = []
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Create a new vehicle submission.
   */
  async function createSubmission(request: VehicleSubmissionRequest) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SubmissionResponse>>('/api/submissions', {
        method: 'POST',
        body: request
      })
      submissions.value.push(response.data)
      return response.data
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to create submission'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Fetch all submissions (admin only).
   */
  async function fetchAllSubmissions(status?: SubmissionStatus) {
    loading.value = true
    error.value = null

    try {
      const query = status ? `?status=${status}` : ''
      const response = await $fetch<ApiResponse<SubmissionResponse[]>>(`/api/admin/submissions${query}`)
      adminSubmissions.value = response.data
      return adminSubmissions.value
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to fetch submissions'
      adminSubmissions.value = []
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Approve a submission (admin/moderator only).
   */
  async function approveSubmission(id: number) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SubmissionResponse>>(`/api/admin/submissions/${id}/approve`, {
        method: 'PUT'
      })
      // Update the submission in the list
      const index = adminSubmissions.value.findIndex(s => s.id === id)
      if (index !== -1) {
        adminSubmissions.value[index] = response.data
      }
      return response.data
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to approve submission'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Reject a submission (admin/moderator only).
   */
  async function rejectSubmission(id: number, feedback?: string) {
    loading.value = true
    error.value = null

    try {
      const response = await $fetch<ApiResponse<SubmissionResponse>>(`/api/admin/submissions/${id}/reject`, {
        method: 'PUT',
        body: { feedback }
      })
      // Update the submission in the list
      const index = adminSubmissions.value.findIndex(s => s.id === id)
      if (index !== -1) {
        adminSubmissions.value[index] = response.data
      }
      return response.data
    } catch (e: any) {
      error.value = e.data?.message || e.message || 'Failed to reject submission'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Get count of submissions by status.
   */
  function getCountByStatus(status: SubmissionStatus): number {
    return adminSubmissions.value.filter(s => s.status === status).length
  }

  /**
   * Clear all submissions from local state.
   */
  function clearSubmissions() {
    submissions.value = []
    adminSubmissions.value = []
    error.value = null
  }

  return {
    submissions,
    adminSubmissions,
    loading,
    error,
    fetchMySubmissions,
    createSubmission,
    fetchAllSubmissions,
    approveSubmission,
    rejectSubmission,
    getCountByStatus,
    clearSubmissions
  }
}
