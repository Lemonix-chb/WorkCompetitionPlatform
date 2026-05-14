<template>
  <div class="teams-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">我的团队</h1>
        <p class="body-text">管理参赛团队，协作共创作品</p>
      </div>

      <!-- Tabs -->
      <div class="tabs-section scale-in mb-lg">
        <div class="tabs-wrapper">
          <div class="tabs-group">
            <button
              :class="['tab-btn', { active: activeTab === 'myTeams' }]"
              @click="activeTab = 'myTeams'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="16" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              我的团队
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'invitations' }]"
              @click="activeTab = 'invitations'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              收到的邀请
              <span v-if="pendingInvitationsCount > 0" class="badge badge-primary">{{ pendingInvitationsCount }}</span>
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'applications' }]"
              @click="activeTab = 'applications'"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16 8L19 8M19 8L19 5M19 8L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              申请加入
            </button>
          </div>
          <button v-if="activeTab === 'myTeams'" class="btn-primary tab-action-btn" @click="showCreateDialog">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            创建团队
          </button>
        </div>
      </div>

      <!-- My Teams Tab -->
      <div v-show="activeTab === 'myTeams'" class="teams-section">
        <!-- Teams List -->
        <div v-if="teams.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="16" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="32" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="24" cy="32" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M16 24V25C16 26.5 19 28 24 28C29 28 32 26.5 32 25V24" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无团队信息</h3>
          <p class="caption">创建团队开始参赛吧</p>
        </div>

        <div v-else class="teams-grid">
          <div v-for="team in teams" :key="team.id" class="team-card">
            <!-- Card Header: Team Info -->
            <div class="card-header">
              <div class="header-content">
                <h3 class="team-name">{{ team.teamName }}</h3>
                <div class="team-meta">
                  <span class="team-code">{{ team.teamCode }}</span>
                  <span :class="getStatusBadgeClass(team.status)" class="status-badge">
                    {{ getStatusText(team.status) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Competition Section -->
            <div class="competition-section">
              <div v-if="team.competitionTrackId" class="competition-card registered">
                <div class="competition-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2L20 7V17L12 22L4 17V7L12 2Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 6L16 8.5V13.5L12 16L8 13.5V8.5L12 6Z" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                </div>
                <div class="competition-info">
                  <div class="competition-label">已报名赛事</div>
                  <div class="competition-name">{{ getCompetitionName(team) }}</div>
                  <div class="track-name">{{ getTrackName(team) }}</div>
                </div>
              </div>
              <div v-else class="competition-card unregistered">
                <div class="competition-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="competition-info">
                  <div class="competition-label">暂未报名</div>
                  <div class="competition-name">等待赛事报名</div>
                </div>
              </div>
            </div>

            <!-- Team Stats -->
            <div class="stats-section">
              <div class="stat-row">
                <div class="stat-item">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="2"/>
                    <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <div class="stat-text">
                    <span class="stat-value">{{ team.currentMemberCount }}</span>
                    <span class="stat-divider">/</span>
                    <span class="stat-max">{{ team.maxMemberCount }}</span>
                    <span class="stat-label">成员</span>
                  </div>
                </div>
                <div class="stat-divider-line"></div>
                <div class="stat-item">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="2"/>
                    <path d="M10 6V10L13 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <div class="stat-text">
                    <span class="stat-value">{{ formatDate(team.createTime) }}</span>
                    <span class="stat-label">创建</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="actions-section">
              <!-- 队长操作 -->
              <template v-if="isTeamLeader(team)">
                <button class="btn-primary" @click="manageTeam(team)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 2L12 5V11L8 14L4 11V5L8 2Z" stroke="currentColor" stroke-width="1.5"/>
                  </svg>
                  管理团队
                </button>
                <button class="btn-secondary" @click="showTeamApplications(team)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <rect x="3" y="3" width="10" height="10" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M6 6H10M6 8H10M6 10H8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  申请
                  <span v-if="team.pendingApplicationsCount > 0" class="badge-dot"></span>
                </button>
                <button
                  v-if="team.currentMemberCount < team.maxMemberCount && team.status === 'FORMING'"
                  class="btn-secondary"
                  @click="openInviteDialog(team)"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 13C3 10.5 5.5 8.5 8 8.5C10.5 8.5 13 10.5 13 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M13 6L15 6M15 6L15 4M15 6L15 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  邀请
                </button>
                <button
                  v-else
                  class="btn-disabled"
                  disabled
                  :title="getInviteDisabledReason(team)"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3 13C3 10.5 5.5 8.5 8 8.5C10.5 8.5 13 10.5 13 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  邀请
                  <span class="disabled-hint">{{ getInviteDisabledReasonShort(team) }}</span>
                </button>
              </template>

              <!-- 成员操作 -->
              <template v-if="!isTeamLeader(team)">
                <button class="btn-primary" @click="viewTeam(team)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 5V8L10 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  查看详情
                </button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- Invitations Tab -->
      <div v-show="activeTab === 'invitations'" class="invitations-section">
        <!-- Invitations List -->
        <div v-if="invitations.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="28" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M8 16H40M16 24H32M16 28H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
              <circle cx="40" cy="12" r="6" fill="#bdc3c7" stroke="none"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无团队邀请</h3>
          <p class="caption">等待其他团队邀请你加入吧</p>
        </div>

        <div v-else class="grid grid-auto">
          <div v-for="invitation in invitations" :key="invitation.id" class="invitation-card card">
            <!-- Invitation Header -->
            <div class="invitation-header">
              <div class="invitation-title-section">
                <h3 class="invitation-name">{{ invitation.teamName || '团队邀请' }}</h3>
              </div>
              <span :class="getInvitationStatusClass(invitation.status)" class="status-badge">
                {{ getInvitationStatusText(invitation.status) }}
              </span>
            </div>

            <!-- Invitation Stats -->
            <div class="invitation-stats">
              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M10 7V10M10 13V13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">邀请人</div>
                  <div class="stat-value">{{ invitation.inviterName || '未知' }}</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M10 6V10L13 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">过期时间</div>
                  <div class="stat-value">{{ formatDateTime(invitation.expireTime) }}</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="stat-content">
                  <div class="stat-label">状态</div>
                  <div class="stat-value status-text">{{ getInvitationStatusText(invitation.status) }}</div>
                </div>
              </div>
            </div>

            <!-- Invitation Actions -->
            <div v-if="invitation.status === 'PENDING'" class="invitation-actions">
              <button class="action-btn-primary" @click="acceptInvitation(invitation)">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M6 9L8 11L12 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>接受邀请</span>
              </button>
              <button class="action-btn-secondary" @click="rejectInvitation(invitation)">
                拒绝
              </button>
            </div>

            <!-- Processed Status -->
            <div v-else class="invitation-processed">
              {{ invitation.status === 'ACCEPTED' ? '已接受' : '已拒绝' }} · {{ formatDateTime(invitation.processTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Applications Tab -->
      <div v-show="activeTab === 'applications'" class="applications-section">
        <!-- Search Section -->
        <div class="search-section card card-flat mb-lg">
          <h3 class="section-title mb-md">搜索团队申请加入</h3>
          <p class="caption mb-md">支持搜索团队名称、团队编号、队长姓名、队长学号或赛道名称</p>
          <div class="search-form flex gap-md">
            <el-input
              v-model="searchTeamKeyword"
              placeholder="输入关键词搜索（团队名/编号/队长/赛道）"
              clearable
              style="width: 400px"
              @keyup.enter="searchTeams"
            />
            <button class="btn-secondary" @click="searchTeams">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              搜索
            </button>
          </div>
        </div>

        <!-- Search Results -->
        <div v-if="searchedTeams.length > 0" class="search-results mb-lg">
          <h3 class="result-title section-title mb-md">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="M10 6V10L13 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            搜索结果（找到 {{ searchedTeams.length }} 个团队）
          </h3>
          <div class="grid grid-auto">
            <div v-for="team in searchedTeams" :key="team.id" class="team-card card card-hover">
              <!-- Team Header -->
              <div class="team-header">
                <div class="team-title-section">
                  <h3 class="team-name">{{ team.teamName }}</h3>
                  <div class="team-code-badge">{{ team.teamCode }}</div>
                </div>
                <span :class="getTeamStatusClass(team.status)" class="status-badge">
                  {{ getTeamStatusText(team.status) }}
                </span>
              </div>

              <!-- Team Stats Grid -->
              <div class="team-stats">
                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <path d="M9 2L16 6V12L9 16L2 12V6L9 2Z" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">赛道</div>
                    <div class="stat-value">{{ team.trackName || '未知' }}</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <circle cx="10" cy="8" r="5" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M4 17C4 14 7 12 10 12C13 12 16 14 16 17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">成员</div>
                    <div class="stat-value">{{ team.currentMemberCount }}/{{ team.maxMemberCount }}</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <circle cx="10" cy="10" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M10 6.5V10M10 13.5V13.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">队长</div>
                    <div class="stat-value">{{ team.leaderName || '未知' }}</div>
                  </div>
                </div>
              </div>

              <!-- Apply Action -->
              <div class="team-actions">
                <button
                  class="action-btn-primary"
                  @click="applyToTeam(team)"
                  v-if="team.currentMemberCount < team.maxMemberCount && team.status === 'FORMING'"
                >
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M4 15C4 12.5 6.5 10.5 9 10.5C11.5 10.5 14 12.5 14 15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M14 7L16 7M16 7L16 5M16 7L16 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span>申请加入</span>
                </button>
                <button
                  v-else
                  class="action-btn-secondary action-btn-disabled"
                  disabled
                  :title="getApplyDisabledReason(team)"
                >
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M6 6L12 12M12 6L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  <span>无法申请</span>
                  <span class="disabled-reason">{{ getApplyDisabledReasonShort(team) }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="searchTeamKeyword && searchedTeams.length === 0 && !searchingStudent" class="empty-result card mb-lg">
          <p class="caption text-center">未找到包含关键词 "{{ searchTeamKeyword }}" 的团队<br/>请尝试其他关键词（团队名/编号/队长姓名/学号/赛道名）</p>
        </div>

        <!-- My Applications -->
        <div v-if="myApplications.length > 0" class="my-applications">
          <h3 class="section-title mb-md">我的申请</h3>
          <div class="grid grid-auto">
            <div v-for="application in myApplications" :key="application.id" class="application-card card">
              <!-- Application Header -->
              <div class="application-header">
                <div class="application-title-section">
                  <h3 class="application-name">申请记录</h3>
                </div>
                <span :class="getApplicationStatusClass(application.status)" class="status-badge">
                  {{ getApplicationStatusText(application.status) }}
                </span>
              </div>

              <!-- Application Stats -->
              <div class="application-stats">
                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <rect x="4" y="4" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M7 7H13M7 10H13M7 13H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">团队名称</div>
                    <div class="stat-value">{{ application.teamName }}</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M10 6V10L13 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">申请时间</div>
                    <div class="stat-value">{{ formatDateTime(application.createTime) }}</div>
                  </div>
                </div>

                <div class="stat-item">
                  <div class="stat-icon">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <circle cx="10" cy="10" r="6" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M10 6.5V10M10 13.5V13.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                  </div>
                  <div class="stat-content">
                    <div class="stat-label">状态</div>
                    <div class="stat-value status-text">{{ getApplicationStatusText(application.status) }}</div>
                  </div>
                </div>
              </div>

              <!-- Cancel Action -->
              <div v-if="application.status === 'PENDING'" class="application-actions">
                <button class="action-btn-secondary" @click="cancelApplication(application)">
                  取消申请
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="searchedTeams.length === 0 && myApplications.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <circle cx="16" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="32" cy="16" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <circle cx="24" cy="32" r="8" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M16 24V25C16 26.5 19 28 24 28C29 28 32 26.5 32 25V24" stroke="#bdc3c7" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">搜索团队申请加入</h3>
          <p class="caption">输入团队名称搜索并申请加入</p>
        </div>
      </div>

      <!-- Create Team Dialog -->
      <el-dialog
        v-model="showCreateTeamDialog"
        title="创建新团队"
        width="480px"
      >
        <el-form :model="createTeamForm" label-width="100px">
          <el-form-item label="团队名称" required>
            <el-input
              v-model="createTeamForm.teamName"
              placeholder="请输入团队名称"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showCreateTeamDialog = false">取消</el-button>
            <el-button type="primary" @click="handleCreateTeam" :loading="loading">
              创建团队
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Invite Member Dialog -->
      <el-dialog
        v-model="showInviteMemberDialog"
        title="邀请成员"
        width="500px"
      >
        <div class="invite-steps">
          <!-- Step 1: Search Student -->
          <div class="step-section">
            <h3 class="section-title mb-md">搜索学生</h3>
            <div class="search-form flex gap-md mb-lg">
              <el-input
                v-model="searchStudentKeyword"
                placeholder="输入学号搜索学生"
                clearable
                @keyup.enter="searchStudent"
              />
              <button class="btn-secondary" @click="searchStudent" :loading="searchingStudent">
                搜索
              </button>
            </div>

            <!-- Search Result -->
            <div v-if="foundStudent" class="student-result card mb-lg">
              <h4 class="result-title caption mb-md">搜索结果</h4>
              <div class="student-info">
                <div class="student-avatar">
                  <span class="avatar-text">{{ foundStudent.realName.charAt(0) }}</span>
                </div>
                <div class="student-details">
                  <h4 class="card-title">{{ foundStudent.realName }}</h4>
                  <div class="student-meta caption">
                    <span>学号：{{ foundStudent.studentNo }}</span>
                    <span v-if="foundStudent.major">专业：{{ foundStudent.major }}</span>
                    <span v-if="foundStudent.college">学院：{{ foundStudent.college }}</span>
                  </div>
                </div>
              </div>
              <div class="student-actions mt-lg">
                <button class="btn-primary" @click="selectStudent">
                  选择此学生
                </button>
              </div>
            </div>
            <div v-else-if="searchedStudents.length > 0" class="students-list mb-lg">
              <h4 class="result-title caption mb-md">搜索结果（{{ searchedStudents.length }} 人）</h4>
              <div v-for="student in searchedStudents" :key="student.id" class="student-option card">
                <div class="student-info">
                  <div class="student-avatar">
                    <span class="avatar-text">{{ student.realName.charAt(0) }}</span>
                  </div>
                  <div class="student-details">
                    <h4 class="card-title">{{ student.realName }}</h4>
                    <div class="student-meta caption">
                      <span>学号：{{ student.studentNo }}</span>
                      <span v-if="student.major">专业：{{ student.major }}</span>
                      <span v-if="student.college">学院：{{ student.college }}</span>
                    </div>
                  </div>
                </div>
                <button class="btn-secondary" @click="selectFoundStudent(student)">
                  选择
                </button>
              </div>
            </div>
            <div v-else-if="searchStudentKeyword && !searchingStudent" class="empty-result caption">
              未找到学号为 "{{ searchStudentKeyword }}" 的学生
            </div>
          </div>

          <!-- Step 2: Confirm Invitation -->
          <div v-if="selectedStudent" class="step-section">
            <h3 class="section-title mb-md">确认邀请</h3>
            <div class="confirmation-box card">
              <div class="info-row">
                <span class="info-label caption">邀请对象</span>
                <span class="info-value body-text">{{ selectedStudent.realName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">学号</span>
                <span class="info-value body-text">{{ selectedStudent.studentNo }}</span>
              </div>
              <div class="info-row">
                <span class="info-label caption">加入团队</span>
                <span class="info-value body-text">{{ invitingTeam?.teamName }}</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="closeInviteDialog">取消</el-button>
            <el-button
              type="primary"
              @click="handleInviteMember"
              :disabled="!selectedStudent"
              :loading="inviting"
            >
              发送邀请
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Team Applications Dialog (队长处理申请) -->
      <el-dialog
        v-model="showApplicationsDialog"
        title="处理团队申请"
        width="700px"
      >
        <div v-if="selectedTeam" class="applications-dialog-content">
          <h3 class="section-title mb-lg">{{ selectedTeam.teamName }} - 待处理申请</h3>

          <div v-if="teamApplications.length === 0" class="empty-state card mb-lg">
            <p class="caption text-center">暂无待处理的申请</p>
          </div>

          <div v-else class="applications-list">
            <div v-for="application in teamApplications" :key="application.id" class="application-item card mb-md">
              <div class="applicant-info">
                <div class="applicant-avatar">
                  <span class="avatar-text">{{ getApplicantName(application).charAt(0) }}</span>
                </div>
                <div class="applicant-details">
                  <h4 class="card-title">{{ getApplicantName(application) }}</h4>
                  <div class="applicant-meta caption">
                    <span>学号：{{ application.applicantStudentNo }}</span>
                    <span v-if="application.message">申请理由：{{ application.message }}</span>
                  </div>
                  <div class="applicant-time caption">
                    申请时间：{{ formatDateTime(application.createTime) }}
                  </div>
                </div>
              </div>

              <div class="application-actions flex gap-md mt-lg">
                <button class="btn-primary" @click="acceptApplication(application)" :loading="processingApplication">
                  <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                    <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="2"/>
                    <path d="M7 10L9 12L13 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  接受申请
                </button>
                <button class="btn-secondary" @click="rejectApplication(application)" :loading="processingApplication">
                  拒绝申请
                </button>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showApplicationsDialog = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, del } from '@/utils/api'
import { showSuccess, showError, showWarning, showConfirm } from '@/utils/messageUtils'
import { formatDateTime, formatDate } from '@/utils/dateUtils'

const router = useRouter()

const activeTab = ref('myTeams')
const teams = ref([])
const tracks = ref([])
const competitions = ref([])
const invitations = ref([])
const myApplications = ref([])
const searchedTeams = ref([])
const searchedStudents = ref([])
const foundStudent = ref(null)
const selectedStudent = ref(null)
const invitingTeam = ref(null)
const selectedTeam = ref(null)
const teamApplications = ref([])
const currentUserId = ref(null)

const searchTeamKeyword = ref('')
const searchStudentKeyword = ref('')

const showCreateTeamDialog = ref(false)
const showInviteMemberDialog = ref(false)
const showApplicationsDialog = ref(false)

const createTeamForm = ref({
  teamName: ''
})

const loading = ref(false)
const searchingStudent = ref(false)
const inviting = ref(false)
const processingApplication = ref(false)

const pendingInvitationsCount = computed(() => {
  return invitations.value.filter(inv => inv.status === 'PENDING').length
})

onMounted(async () => {
  // 获取当前用户ID
  const userId = localStorage.getItem('userId')
  if (userId) {
    currentUserId.value = parseInt(userId)
  }

  await Promise.all([
    fetchTeams(),
    fetchTracks(),
    fetchCompetitions(),
    fetchInvitations(),
    fetchMyApplications()
  ])
})

const fetchTeams = async () => {
  try {
    const data = await get('/teams/my')
    teams.value = data || []
  } catch (error) {
    showError('获取团队列表失败')
  }
}

const fetchTracks = async () => {
  try {
    // 加载所有比赛的赛道数据
    const competitionsData = await get('/competitions')
    const competitionsArray = competitionsData?.records || competitionsData || []

    if (competitionsArray && competitionsArray.length > 0) {
      // 为每个比赛加载赛道
      const trackPromises = competitionsArray.map(comp => get(`/competitions/${comp.id}/tracks`))
      const tracksArrays = await Promise.all(trackPromises)
      const allTracks = tracksArrays.flat()
      tracks.value = allTracks
    } else {
      // 如果没有比赛数据，尝试加载第一个比赛的赛道
      const data = await get('/competitions/1/tracks')
      tracks.value = data || []
    }
  } catch (error) {
    console.error('获取赛道列表失败', error)
  }
}

const fetchCompetitions = async () => {
  try {
    const data = await get('/competitions')
    const competitionsArray = data?.records || data || []
    competitions.value = competitionsArray
  } catch (error) {
    console.error('获取比赛列表失败', error)
  }
}

const fetchInvitations = async () => {
  try {
    const data = await get('/teams/invitations/pending')
    invitations.value = data || []
  } catch (error) {
    console.error('获取邀请列表失败', error)
  }
}

const fetchMyApplications = async () => {
  try {
    const data = await get('/teams/applications/my')
    myApplications.value = data || []
  } catch (error) {
    console.error('获取申请列表失败', error)
  }
}

const showCreateDialog = () => {
  createTeamForm.value = { teamName: '' }
  showCreateTeamDialog.value = true
}

const handleCreateTeam = async () => {
  if (!createTeamForm.value.teamName) {
    showWarning('请输入团队名称')
    return
  }

  loading.value = true

  try {
    const params = new URLSearchParams({
      teamName: createTeamForm.value.teamName
    })

    await post(`/teams?${params.toString()}`)

    showSuccess('团队创建成功')
    showCreateTeamDialog.value = false
    await fetchTeams()
  } catch (error) {
    showError('创建团队失败')
  } finally {
    loading.value = false
  }
}

const manageTeam = (team) => {
  router.push(`/student/teams/${team.id}`)
}

const viewTeam = (team) => {
  router.push(`/student/teams/${team.id}`)
}

const isTeamLeader = (team) => {
  return team.leaderId === currentUserId.value
}

const showTeamApplications = async (team) => {
  selectedTeam.value = team
  await fetchTeamApplications(team.id)
  showApplicationsDialog.value = true
}

const fetchTeamApplications = async (teamId) => {
  try {
    const data = await get(`/teams/${teamId}/applications/pending`)
    teamApplications.value = data || []
  } catch (error) {
    showError('获取申请列表失败')
  }
}

const getApplicantName = (application) => {
  // 返回申请人姓名或学号
  return application.applicantName || application.applicantStudentNo || '未知申请人'
}

const acceptApplication = async (application) => {
  try {
    await post(`/teams/applications/${application.id}/accept`)

    showSuccess('已接受申请')
    await fetchTeamApplications(selectedTeam.value.id)
    await fetchTeams()
  } catch (error) {
    showError('接受申请失败')
  }
}

const rejectApplication = async (application) => {
  try {
    await ElMessageBox.prompt('请输入拒绝理由（可选）', '拒绝申请', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPattern: /.*/,
      inputErrorMessage: ''
    })

    const reason = ElMessageBox.prompt.inputValue || ''
    const params = new URLSearchParams({
      responseReason: reason
    })

    await post(`/teams/applications/${application.id}/reject?${params.toString()}`)

    showSuccess('已拒绝申请')
    await fetchTeamApplications(selectedTeam.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      showError('拒绝申请失败')
    }
  }
}

const openInviteDialog = (team) => {
  invitingTeam.value = team
  searchStudentKeyword.value = ''
  searchedStudents.value = []
  foundStudent.value = null
  selectedStudent.value = null
  showInviteMemberDialog.value = true
}

const closeInviteDialog = () => {
  showInviteMemberDialog.value = false
  invitingTeam.value = null
  selectedStudent.value = null
  foundStudent.value = null
  searchStudentKeyword.value = ''
  searchedStudents.value = []
}

const searchStudent = async () => {
  if (!searchStudentKeyword.value) {
    showWarning('请输入学号')
    return
  }

  searchingStudent.value = true

  try {
    const params = new URLSearchParams({
      keyword: searchStudentKeyword.value
    })

    const data = await get(`/users/students/search?${params.toString()}`)
    searchedStudents.value = data || []

    // 如果只有一个结果,自动选择
    if (searchedStudents.value.length === 1) {
      foundStudent.value = searchedStudents.value[0]
      showSuccess(`找到学生: ${foundStudent.value.realName} (${foundStudent.value.studentNo})`)
    } else if (searchedStudents.value.length > 1) {
      showSuccess(`找到 ${searchedStudents.value.length} 个匹配的学生`)
    } else {
      ElMessage.info('未找到匹配的学生')
    }
  } catch (error) {
    showError('搜索失败')
  } finally {
    searchingStudent.value = false
  }
}

const selectFoundStudent = (student) => {
  foundStudent.value = student
  searchedStudents.value = []
}

const selectStudent = () => {
  if (foundStudent.value) {
    selectedStudent.value = foundStudent.value
    foundStudent.value = null
  }
}

const handleInviteMember = async () => {
  if (!selectedStudent.value) {
    showWarning('请选择要邀请的学生')
    return
  }

  inviting.value = true

  try {
    const params = new URLSearchParams({
      inviteeStudentNo: selectedStudent.value.studentNo
    })

    await post(`/teams/${invitingTeam.value.id}/invite?${params.toString()}`)

    showSuccess('邀请已发送')
    closeInviteDialog()
    await fetchTeams()
  } catch (error) {
    showError('邀请失败')
  } finally {
    inviting.value = false
  }
}

const searchTeams = async () => {
  if (!searchTeamKeyword.value) {
    showWarning('请输入团队名称')
    return
  }

  try {
    const params = new URLSearchParams({
      keyword: searchTeamKeyword.value
    })

    const data = await get(`/teams/search?${params.toString()}`)
    searchedTeams.value = data || []

    if (searchedTeams.value.length > 0) {
      showSuccess(`找到 ${searchedTeams.value.length} 个匹配的团队`)
    } else {
      ElMessage.info('未找到匹配的团队')
    }
  } catch (error) {
    showError('搜索失败')
  }
}

const applyToTeam = async (team) => {
  try {
    await post(`/teams/${team.id}/apply`)

    showSuccess('申请已发送')
    await fetchMyApplications()
  } catch (error) {
    showError('申请失败')
  }
}

const cancelApplication = async (application) => {
  try {
    await showConfirm('确定要取消此申请吗？', '取消申请')

    await del(`/teams/applications/${application.id}`)

    showSuccess('申请已取消')
    await fetchMyApplications()
  } catch (error) {
    if (error !== 'cancel') {
      showError('取消失败')
    }
  }
}

const acceptInvitation = async (invitation) => {
  try {
    await post(`/teams/invitations/${invitation.id}/accept`)

    showSuccess('已接受邀请')
    await Promise.all([fetchInvitations(), fetchTeams()])
  } catch (error) {
    showError('接受邀请失败')
  }
}

const rejectInvitation = async (invitation) => {
  try {
    await showConfirm('确定要拒绝这个邀请吗？', '拒绝邀请')

    await post(`/teams/invitations/${invitation.id}/reject`)

    showSuccess('已拒绝邀请')
    await fetchInvitations()
  } catch (error) {
    if (error !== 'cancel') {
      showError('拒绝邀请失败')
    }
  }
}

const getTrackName = (team) => {
  if (!team.competitionTrackId) return '待报名时选择'
  const track = tracks.value.find(t => t.id === team.competitionTrackId)
  return track ? track.trackName : '未知赛道'
}

const getCompetitionName = (team) => {
  if (!team.competitionTrackId) return '未报名赛事'
  // 先找到track，再通过track找到competition
  const track = tracks.value.find(t => t.id === team.competitionTrackId)
  if (!track) return '未知比赛'

  const competition = competitions.value.find(c => c.id === track.competitionId)
  return competition ? competition.competitionName : '未知比赛'
}

const getTeamStatusClass = (status) => {
  return {
    'badge-primary': status === 'CONFIRMED' || status === 'REGISTERED',
    'badge-warning': status === 'FORMING',
    'badge-gray': status === 'SUBMITTED' || status === 'REVIEWED' || status === 'AWARDED'
  }
}

const getTeamStatusText = (status) => {
  const texts = {
    FORMING: '组建中',
    CONFIRMED: '已确认',
    REGISTERED: '已报名',
    SUBMITTED: '已提交',
    REVIEWED: '已评审',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

// 新的状态badge辅助方法
const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'CONFIRMED':
    case 'REGISTERED':
      return 'badge-success'
    case 'FORMING':
      return 'badge-warning'
    default:
      return 'badge-info'
  }
}

const getStatusText = (status) => {
  const texts = {
    FORMING: '组建中',
    CONFIRMED: '已确认',
    REGISTERED: '已报名',
    SUBMITTED: '已提交',
    REVIEWED: '已评审',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

const getInviteDisabledReason = (team) => {
  if (team.status !== 'FORMING') {
    return '团队已确认锁定，无法邀请新成员'
  }
  if (team.currentMemberCount >= team.maxMemberCount) {
    return '团队成员已满，无法邀请新成员'
  }
  return '无法邀请成员'
}

const getInviteDisabledReasonShort = (team) => {
  if (team.status !== 'FORMING') {
    return '(已锁定)'
  }
  if (team.currentMemberCount >= team.maxMemberCount) {
    return '(已满员)'
  }
  return ''
}

const getInvitationStatusClass = (status) => {
  return {
    'badge-primary': status === 'ACCEPTED',
    'badge-warning': status === 'PENDING',
    'badge-gray': status === 'REJECTED' || status === 'EXPIRED' || status === 'CANCELLED'
  }
}

const getInvitationStatusText = (status) => {
  const texts = {
    PENDING: '待处理',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    EXPIRED: '已过期',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const getApplicationStatusClass = (status) => {
  return {
    'badge-primary': status === 'ACCEPTED',
    'badge-warning': status === 'PENDING',
    'badge-gray': status === 'REJECTED'
  }
}

const getApplicationStatusText = (status) => {
  const texts = {
    PENDING: '待评审',
    ACCEPTED: '已通过',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}

const getApplyDisabledReason = (team) => {
  if (team.status !== 'FORMING') {
    return '团队已确认锁定，无法申请加入'
  }
  if (team.currentMemberCount >= team.maxMemberCount) {
    return '团队成员已满，无法申请加入'
  }
  return '无法申请加入此团队'
}

const getApplyDisabledReasonShort = (team) => {
  if (team.status !== 'FORMING') {
    return '(已锁定)'
  }
  if (team.currentMemberCount >= team.maxMemberCount) {
    return '(已满员)'
  }
  return ''
}

</script>

<style scoped>
.teams-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.tabs-section {
  padding: var(--spacing-lg);
}

.tabs-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-lg);
  flex-wrap: wrap;
}

.tabs-group {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.tab-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  background: var(--color-surface-light);
}

.tab-btn.active {
  background: var(--color-accent);
  color: white;
  border-color: var(--color-accent);
}

.tab-action-btn {
  flex-shrink: 0;
  padding: 8px 16px !important;
  font-size: 13px !important;
  border-radius: 10px !important;
}

.tab-action-btn svg {
  width: 16px !important;
  height: 16px !important;
}

/* === Modern Team Card Design === */

.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: var(--spacing-md);
}

.team-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.06);
  height: 360px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.team-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #0071e3 0%, #00c7be 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.team-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(0, 113, 227, 0.12);
  border-color: rgba(0, 113, 227, 0.15);
}

.team-card:hover::before {
  opacity: 1;
}

/* Card Header */
.card-header {
  padding: 20px 20px 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.team-name {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: #1d1d1f;
  margin: 0;
  line-height: 1.3;
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.team-code {
  font-family: 'SF Mono', 'SF Pro Text', monospace;
  font-size: 12px;
  font-weight: 500;
  color: #86868b;
  letter-spacing: 0.3px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-family: 'SF Pro Text', -apple-system, sans-serif;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.2px;
  display: inline-flex;
  align-items: center;
}

.badge-success {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
  color: white;
}

.badge-warning {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.badge-info {
  background: rgba(0, 113, 227, 0.12);
  color: #0071e3;
  border: 1px solid rgba(0, 113, 227, 0.2);
}

/* Competition Section */
.competition-section {
  padding: 0 20px 16px 20px;
}

.competition-card {
  padding: 14px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 60px;
}

.competition-card.registered {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.competition-card.unregistered {
  background: rgba(142, 142, 147, 0.05);
  border: 1px solid rgba(142, 142, 147, 0.12);
}

.competition-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.competition-card.registered .competition-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.competition-card.unregistered .competition-icon {
  background: rgba(142, 142, 147, 0.12);
  color: #86868b;
}

.competition-info {
  flex: 1;
  min-width: 0;
}

.competition-label {
  font-family: 'SF Pro Text', -apple-system, sans-serif;
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  margin-bottom: 2px;
}

.competition-card.registered .competition-label {
  color: rgba(102, 126, 234, 0.7);
}

.competition-card.unregistered .competition-label {
  color: rgba(142, 142, 147, 0.7);
}

.competition-name {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: -0.3px;
  margin-bottom: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.competition-card.registered .competition-name {
  color: #1d1d1f;
}

.competition-card.unregistered .competition-name {
  color: #86868b;
}

.track-name {
  font-family: 'SF Pro Text', -apple-system, sans-serif;
  font-size: 12px;
  color: #0071e3;
  font-weight: 500;
}

/* Stats Section */
.stats-section {
  padding: 0 20px 12px 20px;
  flex: 1;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 10px;
  height: 42px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.stat-item svg {
  color: #0071e3;
  flex-shrink: 0;
  width: 16px;
  height: 16px;
}

.stat-text {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 3px;
}

.stat-value {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.2px;
}

.stat-divider {
  color: #86868b;
  font-weight: 400;
  margin: 0 1px;
  font-size: 12px;
}

.stat-max {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #86868b;
  letter-spacing: -0.2px;
}

.stat-label {
  font-family: 'SF Pro Text', -apple-system, sans-serif;
  font-size: 10px;
  color: #86868b;
  letter-spacing: 0.2px;
  margin-left: 3px;
}

.stat-divider-line {
  width: 1px;
  height: 28px;
  background: rgba(0, 0, 0, 0.08);
}

.stat-value {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.2px;
}

.stat-divider {
  color: #86868b;
  font-weight: 400;
  margin: 0 2px;
}

.stat-max {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #86868b;
  letter-spacing: -0.2px;
}

.stat-label {
  font-family: 'SF Pro Text', -apple-system, sans-serif;
  font-size: 11px;
  color: #86868b;
  letter-spacing: 0.2px;
  margin-top: 2px;
}

.stat-divider-line {
  width: 1px;
  height: 32px;
  background: rgba(0, 0, 0, 0.06);
}

/* Actions Section */
.actions-section {
  padding: 12px 20px 16px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(0, 0, 0, 0.01);
  display: flex;
  gap: 6px;
  margin-top: auto;
}

.btn-primary {
  flex: 1;
  min-width: 0;
  padding: 8px 12px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #0071e3 0%, #0077ed 100%);
  color: white;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  position: relative;
  overflow: hidden;
  white-space: nowrap;
}

.btn-primary::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 113, 227, 0.2);
}

.btn-primary:hover::before {
  opacity: 1;
}

.btn-secondary {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(0, 113, 227, 0.2);
  background: rgba(0, 113, 227, 0.04);
  color: #0071e3;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  position: relative;
  white-space: nowrap;
}

.btn-secondary:hover {
  background: rgba(0, 113, 227, 0.08);
  border-color: rgba(0, 113, 227, 0.3);
  transform: translateY(-2px);
}

.btn-disabled {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(142, 142, 147, 0.15);
  background: rgba(142, 142, 147, 0.06);
  color: #86868b;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: not-allowed;
  opacity: 0.6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  white-space: nowrap;
}

.disabled-hint {
  font-size: 10px;
  color: rgba(142, 142, 147, 0.8);
  font-weight: 500;
  margin-left: 3px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fbbf24;
  position: absolute;
  top: -3px;
  right: -3px;
  border: 1.5px solid white;
  animation: pulse 2s ease infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* === Responsive Design === */
@media (max-width: 768px) {
  .teams-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .team-card {
    height: auto;
    min-height: 360px;
  }

  .actions-section {
    flex-direction: column;
  }

  .actions-section button {
    width: 100%;
  }

  .stat-row {
    flex-direction: column;
    gap: 10px;
  }

  .stat-divider-line {
    width: 100%;
    height: 1px;
  }
}

/* === Invitation & Application Cards === */

.invitation-card,
.application-card {
  padding: 0;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.invitation-card:hover,
.application-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.invitation-header,
.application-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 24px 20px 24px;
  gap: 16px;
}

.invitation-title-section,
.application-title-section {
  flex: 1;
}

.invitation-name,
.application-name {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: #1d1d1f;
  margin: 0;
}

.invitation-stats,
.application-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 0 24px 24px 24px;
}

.invitation-actions,
.application-actions {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(0, 0, 0, 0.01);
}

.invitation-processed {
  padding: 16px 24px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 0;
  text-align: center;
  color: #86868b;
  font-family: 'SF Pro Text', -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: 13px;
}

/* Other sections remain unchanged */
.team-brief-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.brief-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
}

.empty-result {
  padding: var(--spacing-lg);
  background: rgba(0, 0, 0, 0.02);
  border-radius: var(--radius-md);
}

.student-result,
.student-option {
  padding: var(--spacing-lg);
}

.student-info {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.student-avatar {
  width: 48px;
  height: 48px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.student-details {
  flex: 1;
}

.student-meta {
  display: flex;
  gap: var(--spacing-md);
  color: var(--color-text-secondary);
}

.student-actions {
  margin-top: var(--spacing-md);
}

.students-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.student-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.confirmation-box {
  padding: var(--spacing-lg);
}

.applications-dialog-content {
  padding: var(--spacing-md);
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.application-item {
  padding: var(--spacing-lg);
}

.applicant-info {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-start;
}

.applicant-avatar {
  width: 48px;
  height: 48px;
  background: var(--color-accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.applicant-details {
  flex: 1;
}

.applicant-meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
}

.applicant-time {
  color: var(--color-text-light);
  margin-top: var(--spacing-xs);
}

.ml-sm {
  margin-left: 8px;
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

@media (max-width: 768px) {
  .tabs-wrapper {
    flex-direction: column;
    align-items: stretch;
  }

  .tabs-group {
    flex-direction: column;
  }

  .tab-btn {
    width: 100%;
    justify-content: center;
  }

  .tab-action-btn {
    width: 100%;
    justify-content: center;
  }

  .team-actions button,
  .invitation-actions button,
  .application-actions button {
    width: 100%;
    justify-content: center;
  }

  .search-form > * {
    width: 100%;
  }
}
</style>