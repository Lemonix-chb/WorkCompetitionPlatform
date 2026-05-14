<template>
  <div class="works-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header slide-in">
        <h1 class="page-title">我的作品</h1>
        <p class="body-text">管理和提交参赛作品</p>
      </div>

      <!-- Action Bar -->
      <div class="action-bar scale-in mb-lg">
        <button class="btn-primary" @click="showCreateDialog">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M10 4V16M4 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          创建新作品
        </button>
      </div>

      <!-- Works List -->
      <div class="works-section">
        <!-- Loading -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
        </div>

        <!-- Empty State -->
        <div v-else-if="works.length === 0" class="empty-state card">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect x="8" y="8" width="32" height="24" rx="3" stroke="#bdc3c7" stroke-width="2"/>
              <path d="M12 16H36M12 20H36M12 24H28" stroke="#bdc3c7" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <h3 class="card-title mb-md">暂无作品</h3>
          <p class="caption">创建团队后即可开始制作作品</p>
        </div>

        <!-- Dashboard-Style Works Grid -->
        <div v-else class="works-dashboard">
          <div
            v-for="work in works"
            :key="work.id"
            class="work-dashboard-card"
            :data-type="work.workType"
            :class="{
              'awarded': work.developmentStatus === 'AWARDED',
              'score-high': work.aiReviewStatus?.overallScore >= 80,
              'score-medium': work.aiReviewStatus?.overallScore >= 60 && work.aiReviewStatus?.overallScore < 80,
              'score-low': work.aiReviewStatus?.overallScore < 60
            }"
          >
            <!-- Left AI Score Bar (80px width) -->
            <div
              class="ai-score-bar"
              :class="{
                'reviewing': work.aiReviewStatus?.status === 'VALIDATING',
                'invalid': work.aiReviewStatus?.status === 'INVALID' && !work.aiReviewStatus?.overallScore
              }"
              :style="getScoreGradient(work.aiReviewStatus?.overallScore, work.aiReviewStatus?.status)"
            >
              <div v-if="work.aiReviewStatus?.status === 'VALIDATING'" class="review-animation">
                <div class="review-spinner"></div>
                <div class="review-text">评审中</div>
              </div>
              <div v-else>
                <div class="score-value">
                  {{ work.aiReviewStatus?.overallScore || '--' }}
                </div>
                <div class="score-label">AI评分</div>
              </div>
            </div>

            <!-- Main Content Area -->
            <div class="work-main">
              <!-- Top Progress Flow Bar -->
              <div class="progress-flow">
                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 1, work.aiReviewStatus)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">制作中</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 1)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 2, work.aiReviewStatus)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">已完成</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 2)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 3, work.aiReviewStatus)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">已提交</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 3)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 4, work.aiReviewStatus)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">已获奖</span>
                </div>
              </div>

              <!-- Work Details -->
              <div class="work-details">
                <div class="work-title-row">
                  <h3 class="work-title">{{ work.workName }}</h3>
                  <span class="type-badge" :class="'type-' + work.workType">
                    {{ getTypeIcon(work.workType) }} {{ getWorkTypeText(work.workType) }}
                  </span>
                </div>

                <div class="work-meta">
                  <div class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                      <rect x="2" y="2" width="10" height="10" rx="1" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    <span>{{ work.workCode }}</span>
                  </div>
                  <div class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                      <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5"/>
                      <path d="M7 4V7L9 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    <span>创建于 {{ formatDate(work.createTime) }}</span>
                  </div>
                </div>

                <!-- AI Review Status -->
                <div v-if="work.aiReviewStatus" class="review-status-section">
                  <div class="review-type-item">
                    <span class="review-type-label">🤖 AI评审</span>
                    <span :class="getAIStatusClass(work.aiReviewStatus.status)" class="status-mini-badge">
                      {{ getAIStatusText(work.aiReviewStatus.status) }}
                    </span>
                    <button
                      v-if="work.aiReviewStatus.status !== 'PENDING' && work.aiReviewStatus.status !== 'VALIDATING'"
                      class="view-report-btn"
                      @click="viewAIReport(work)"
                    >
                      查看报告
                    </button>
                    <button
                      v-if="work.aiReviewStatus.status === 'VALIDATING'"
                      class="refresh-btn"
                      @click="refreshAIStatus(work)"
                    >
                      刷新状态
                    </button>
                  </div>

                  <!-- Judge Review Status -->
                  <div v-if="work.judgeReviewStatus" class="review-type-item">
                    <span class="review-type-label">👨‍⚖️ 评审员评审</span>
                    <span :class="getJudgeStatusClass(work.judgeReviewStatus.status)" class="status-mini-badge">
                      {{ getJudgeStatusText(work.judgeReviewStatus.status) }}
                    </span>
                    <span v-if="work.judgeReviewStatus.judgeCount > 0" class="judge-count-badge">
                      {{ work.judgeReviewStatus.judgeCount }} 人
                    </span>
                    <button
                      v-if="work.judgeReviewStatus.status === 'COMPLETED'"
                      class="view-report-btn"
                      @click="viewJudgeReport(work)"
                    >
                      查看报告
                    </button>
                  </div>
                </div>

                <!-- Attachments List -->
                <div v-if="getWorkAttachments(work.id).length > 0" class="attachments-mini">
                  <div class="attachments-header">
                    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                      <path d="M4 4H10M6 4V10C6 11 7 12 8 12H10C11 12 12 11 12 10V4" stroke="currentColor" stroke-width="1.5"/>
                    </svg>
                    <span class="attachments-label">已上传文件</span>
                    <span class="attachments-count">{{ getWorkAttachments(work.id).length }}</span>
                  </div>
                  <div class="attachments-items">
                    <div
                      v-for="attachment in getWorkAttachments(work.id)"
                      :key="attachment.id"
                      class="attachment-item-mini"
                    >
                      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                        <rect x="2" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/>
                      </svg>
                      <span class="attachment-name">{{ attachment.fileName }}</span>
                      <span class="attachment-size">{{ formatFileSize(attachment.fileSize) }}</span>
                      <button
                        class="delete-attachment-btn"
                        @click="deleteAttachment(attachment)"
                        title="删除文件"
                      >
                        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                          <path d="M2 2L10 10M10 2L2 10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Time Warning Banner -->
              <div v-if="shouldShowTimeBanner(work)" class="time-banner" :class="getTimeBannerLevel(work)">
                {{ getTimeBannerMessage(work) }}
              </div>

              <!-- Work Actions -->
              <div class="work-actions-bar">
                <button class="action-btn" @click="editWork(work)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 14L14 2M14 2H8M14 2V8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  编辑
                </button>

                <button class="action-btn" @click="showUploadDialog(work)">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 2V14M4 8L8 4L12 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  上传
                </button>

                <button
                  v-if="work.developmentStatus === 'IN_PROGRESS'"
                  class="action-btn action-btn-complete"
                  @click="markCompleted(work)"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M5 8L7 10L11 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  完成
                </button>

                <button
                  v-if="work.developmentStatus === 'COMPLETED'"
                  class="action-btn action-btn-submit"
                  @click="submitWork(work)"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M8 2V14M4 8L8 4L12 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  提交
                </button>

                <button
                  v-if="work.developmentStatus === 'SUBMITTED'"
                  class="action-btn action-btn-resubmit"
                  @click="submitWork(work)"
                >
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M14 8L8 14M8 14L2 8M8 14V2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                  重新提交
                </button>
              </div>
            </div>

            <!-- Award Trophy Icon -->
            <div v-if="work.developmentStatus === 'AWARDED'" class="award-trophy">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
                <path d="M16 4L20 12L28 12L22 18L24 26L16 22L8 26L10 18L4 12L12 12L16 4Z" fill="#FFD700" stroke="#FFA500" stroke-width="2"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Work Dialog -->
      <el-dialog
        v-model="showCreateWorkDialog"
        title="创建新作品"
        width="600px"
      >
        <el-form :model="createWorkForm" label-width="100px">
          <el-form-item label="作品名称" required>
            <el-input
              v-model="createWorkForm.workName"
              placeholder="请输入作品名称"
            />
          </el-form-item>
          <el-form-item label="选择团队" required>
            <el-select
              v-model="createWorkForm.teamId"
              placeholder="请选择团队"
              @change="handleTeamChange"
            >
              <el-option
                v-for="team in teams"
                :key="team.id"
                :label="team.teamName"
                :value="team.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="作品简介">
            <el-input
              v-model="createWorkForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入作品简介"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showCreateWorkDialog = false">取消</el-button>
            <el-button type="primary" @click="handleCreateWork" :loading="creating">
              创建作品
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Edit Work Dialog -->
      <el-dialog
        v-model="showEditWorkDialog"
        title="编辑作品"
        width="600px"
      >
        <el-form :model="editWorkForm" label-width="100px">
          <el-form-item label="作品名称" required>
            <el-input
              v-model="editWorkForm.workName"
              placeholder="请输入作品名称"
            />
          </el-form-item>
          <el-form-item label="作品简介">
            <el-input
              v-model="editWorkForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入作品简介"
            />
          </el-form-item>
          <el-form-item label="创新点说明">
            <el-input
              v-model="editWorkForm.innovationPoints"
              type="textarea"
              :rows="2"
              placeholder="请描述作品的创新点"
            />
          </el-form-item>
          <el-form-item label="关键功能">
            <el-input
              v-model="editWorkForm.keyFeatures"
              type="textarea"
              :rows="2"
              placeholder="请描述关键功能特性"
            />
          </el-form-item>
          <el-form-item label="技术栈" v-if="editWorkForm.workType === 'CODE'">
            <el-input
              v-model="editWorkForm.techStack"
              placeholder="请输入使用的技术栈"
            />
          </el-form-item>
          <el-form-item label="团队分工">
            <el-input
              v-model="editWorkForm.divisionOfLabor"
              type="textarea"
              :rows="2"
              placeholder="请描述团队成员分工"
            />
          </el-form-item>
          <el-form-item label="目标用户">
            <el-input
              v-model="editWorkForm.targetUsers"
              placeholder="请描述目标用户或应用场景"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showEditWorkDialog = false">取消</el-button>
            <el-button type="primary" @click="handleUpdateWork" :loading="updating">
              保存修改
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Upload File Dialog -->
      <el-dialog
        v-model="showUploadFileDialog"
        title="上传作品文件"
        width="500px"
      >
        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="选择文件" required>
            <input
              type="file"
              @change="handleFileSelect"
              class="file-input"
            />
            <div v-if="uploadForm.file" class="selected-file">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M4 4H16M6 4V16C6 17 7 18 8 18H12C13 18 14 17 14 16V4" stroke="currentColor" stroke-width="2"/>
              </svg>
              <span>{{ uploadForm.file.name }}</span>
              <span class="file-size caption">{{ formatFileSize(uploadForm.file.size) }}</span>
            </div>
          </el-form-item>
          <el-form-item label="文件类型" required>
            <el-radio-group v-model="uploadForm.attachmentType">
              <el-radio label="SOURCE">源代码</el-radio>
              <el-radio label="DOCUMENT">文档</el-radio>
              <el-radio label="DEMO">演示视频</el-radio>
              <el-radio label="OTHER">其他</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showUploadFileDialog = false">取消</el-button>
            <el-button type="primary" @click="handleUploadFile" :loading="uploading">
              开始上传
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- AI Review Report Dialog - Beautified -->
      <el-dialog
        v-model="showAIReportDialog"
        title="AI智能评审报告"
        width="90%"
        :close-on-click-modal="false"
      >
        <div v-if="currentAIReport" class="ai-report-beautified">
          <!-- 综合得分展示 -->
          <div class="report-score-hero">
            <div class="score-circle-container">
              <div class="score-ring-big" :style="getScoreRingStyle(currentAIReport.overallScore)">
                <div class="score-center">
                  <div class="big-score-number">{{ currentAIReport.overallScore || 0 }}</div>
                  <div class="score-label-text">综合评分</div>
                </div>
              </div>
            </div>

            <!-- 维度评分 - 根据作品类型动态显示 -->
            <div class="dimension-bars">
              <!-- CODE作品评分维度 -->
              <div v-if="currentAIReport.workType === 'CODE'">
                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">💡</span>
                    <span class="dim-title">创新性</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill innovation" :style="{ width: (currentAIReport.innovationScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.innovationScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🔧</span>
                    <span class="dim-title">实用性</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill practicality" :style="{ width: (currentAIReport.practicalityScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.practicalityScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">✨</span>
                    <span class="dim-title">用户体验</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill experience" :style="{ width: (currentAIReport.userExperienceScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.userExperienceScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">📖</span>
                    <span class="dim-title">文档质量</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill documentation" :style="{ width: (currentAIReport.documentationScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.documentationScore || 0 }} / 25</span>
                </div>
              </div>

              <!-- PPT作品评分维度 -->
              <div v-if="currentAIReport.workType === 'PPT'">
                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🎨</span>
                    <span class="dim-title">创意</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill creativity" :style="{ width: (currentAIReport.creativityScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.creativityScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">👁️</span>
                    <span class="dim-title">视觉效果</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill visual" :style="{ width: (currentAIReport.visualEffectScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.visualEffectScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">📊</span>
                    <span class="dim-title">内容呈现</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill content" :style="{ width: (currentAIReport.contentPresentationScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.contentPresentationScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🌟</span>
                    <span class="dim-title">原创性</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill originality" :style="{ width: (currentAIReport.originalityScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.originalityScore || 0 }} / 25</span>
                </div>
              </div>

              <!-- VIDEO作品评分维度 -->
              <div v-if="currentAIReport.workType === 'VIDEO'">
                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">📖</span>
                    <span class="dim-title">故事性</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill story" :style="{ width: (currentAIReport.storyScore / 30 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.storyScore || 0 }} / 30</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🎬</span>
                    <span class="dim-title">视觉效果</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill visual" :style="{ width: (currentAIReport.visualEffectScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.visualEffectScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🎯</span>
                    <span class="dim-title">导演技巧</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill director" :style="{ width: (currentAIReport.directorSkillScore / 25 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.directorSkillScore || 0 }} / 25</span>
                </div>

                <div class="dimension-row">
                  <div class="dim-info">
                    <span class="dim-emoji">🌟</span>
                    <span class="dim-title">原创性</span>
                  </div>
                  <div class="dim-bar-wrapper">
                    <div class="dim-bar-fill originality" :style="{ width: (currentAIReport.originalityScore / 20 * 100) + '%' }"></div>
                  </div>
                  <span class="dim-score">{{ currentAIReport.originalityScore || 0 }} / 20</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 指标卡片 -->
          <div class="metrics-grid">
            <div class="metric-box">
              <div class="metric-icon-box risk-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L22 20H2L12 2Z" stroke="currentColor" stroke-width="2"/>
                </svg>
              </div>
              <div class="metric-content">
                <div class="metric-title">风险等级</div>
                <div class="metric-display" :class="'risk-' + (currentAIReport.riskLevel || 'LOW')">
                  {{ currentAIReport.riskLevel || 'LOW' }}
                </div>
              </div>
            </div>

            <div class="metric-box">
              <div class="metric-icon-box complex-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2"/>
                </svg>
              </div>
              <div class="metric-content">
                <div class="metric-title">复杂度</div>
                <div class="metric-display" :class="'complex-' + (currentAIReport.complexityLevel || 'LOW')">
                  {{ currentAIReport.complexityLevel || 'LOW' }}
                </div>
              </div>
            </div>

            <div v-if="currentAIReport.codeQualityMetrics" class="metric-box">
              <div class="metric-icon-box duplicate-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                  <path d="M8 8H16V16H8V8Z" stroke="currentColor" stroke-width="2"/>
                  <path d="M10 10H18V18H10V10Z" stroke="currentColor" stroke-width="2" opacity="0.5"/>
                </svg>
              </div>
              <div class="metric-content">
                <div class="metric-title">重复率</div>
                <div class="metric-display duplicate-value">
                  {{ currentAIReport.codeQualityMetrics.duplicateRate }}%
                </div>
              </div>
            </div>
          </div>

          <!-- 评审摘要 -->
          <div v-if="currentAIReport.reviewSummary" class="summary-box">
            <div class="summary-header">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5"/>
                <path d="M10 5V10L13 12" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <h3>评审摘要</h3>
            </div>
            <div class="summary-body">
              <p>{{ currentAIReport.reviewSummary }}</p>
            </div>
          </div>

          <!-- 改进建议 -->
          <div v-if="currentAIReport.improvementSuggestions && currentAIReport.improvementSuggestions.length > 0" class="suggestions-box">
            <div class="suggestions-header">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M9 11L11 13L15 9" stroke="currentColor" stroke-width="1.5"/>
                <path d="M3 11L5 13L9 9" stroke="currentColor" stroke-width="1.5" opacity="0.3"/>
              </svg>
              <h3>改进建议</h3>
            </div>
            <div class="suggestions-list">
              <div v-for="(suggestion, index) in currentAIReport.improvementSuggestions" :key="index" class="suggestion-item">
                <div class="suggestion-number">{{ index + 1 }}</div>
                <div class="suggestion-text">{{ suggestion }}</div>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button class="report-close-btn" @click="showAIReportDialog = false">关闭报告</el-button>
        </template>
      </el-dialog>

      <!-- Judge Review Report Dialog -->
      <el-dialog
        v-model="showJudgeReportDialog"
        title="评审员评审报告"
        width="700px"
      >
        <div v-if="currentJudgeReport" class="judge-report-content">
          <div class="report-header mb-lg">
            <div class="main-score">
              <span class="score-number">{{ currentJudgeReport.totalScore || 0 }}</span>
              <span class="score-unit caption">/100</span>
            </div>
            <span class="badge-lg" :class="getJudgeScoreClass(currentJudgeReport.totalScore)">
              {{ getJudgeScoreText(currentJudgeReport.totalScore) }}
            </span>
          </div>

          <div class="judge-info mb-lg">
            <div class="info-item">
              <span class="info-label caption">评审员</span>
              <span class="info-value">{{ currentJudgeReport.judgeName || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label caption">评审员数量</span>
              <span class="info-value">{{ currentJudgeReport.judgeCount || 1 }} 人</span>
            </div>
            <div class="info-item">
              <span class="info-label caption">评审时间</span>
              <span class="info-value">{{ formatDate(currentJudgeReport.reviewTime) }}</span>
            </div>
          </div>

          <div class="score-details mb-lg">
            <div v-for="item in currentJudgeReport.scoreItems" :key="item.id" class="score-item">
              <span class="item-label caption">{{ item.criteriaName }}</span>
              <div class="score-bar">
                <div class="score-bar-fill" :style="{ width: (item.score / item.maxScore * 100) + '%' }"></div>
              </div>
              <span class="item-value body-text">{{ item.score }}/{{ item.maxScore }}</span>
            </div>
          </div>

          <div class="review-comments-section mb-lg">
            <h4 class="section-title mb-md">评审意见</h4>
            <p class="caption">{{ currentJudgeReport.comments || '暂无评审意见' }}</p>
          </div>

          <div v-if="currentJudgeReport.strengths && currentJudgeReport.strengths.length > 0" class="strengths-section mb-lg">
            <h4 class="section-title mb-md">作品优点</h4>
            <ul class="suggestions-list caption">
              <li v-for="(strength, index) in currentJudgeReport.strengths" :key="index">{{ strength }}</li>
            </ul>
          </div>

          <div v-if="currentJudgeReport.weaknesses && currentJudgeReport.weaknesses.length > 0" class="weaknesses-section">
            <h4 class="section-title mb-md">改进建议</h4>
            <ul class="suggestions-list caption">
              <li v-for="(weakness, index) in currentJudgeReport.weaknesses" :key="index">{{ weakness }}</li>
            </ul>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showJudgeReportDialog = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del, uploadFile } from '@/utils/api'
import { showSuccess, showError, showWarning, showConfirm, showInfo } from '@/utils/messageUtils'
import { formatDateTime } from '@/utils/dateUtils'

const router = useRouter()

const works = ref([])
const teams = ref([])
const workAttachments = ref({})
const loading = ref(false)
const creating = ref(false)
const updating = ref(false)
const uploading = ref(false)
const timeStatusMap = ref({})

const showCreateWorkDialog = ref(false)
const showEditWorkDialog = ref(false)
const showUploadFileDialog = ref(false)
const showAIReportDialog = ref(false)
const showJudgeReportDialog = ref(false)

const currentAIReport = ref(null)
const currentJudgeReport = ref(null)
const pollingIntervals = ref({})

const createWorkForm = ref({
  workName: '',
  teamId: '',
  competitionId: 1,
  trackId: '',
  description: ''
})

const editWorkForm = ref({
  id: '',
  workName: '',
  description: '',
  innovationPoints: '',
  keyFeatures: '',
  techStack: '',
  divisionOfLabor: '',
  targetUsers: '',
  workType: ''
})

const uploadForm = ref({
  workId: '',
  file: null,
  attachmentType: 'SOURCE'
})

onMounted(async () => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('userRole')

  if (!token || role !== 'STUDENT') {
    showWarning('您没有权限访问此页面')
    router.push('/login')
    return
  }

  await Promise.all([
    fetchWorks(),
    fetchTeams()
  ])

  for (const work of works.value) {
    await fetchWorkAttachments(work.id)
  }

  await fetchAllTimeStatuses()
})

const fetchAllTimeStatuses = async () => {
  const promises = works.value.map(work =>
    get(`/competitions/${work.competitionId}/time-status`)
      .then(status => {
        timeStatusMap.value[work.competitionId] = status
      })
      .catch(error => {
        console.error(`Failed to fetch time status for competition ${work.competitionId}`, error)
      })
  )
  await Promise.all(promises)
}

const fetchWorks = async () => {
  loading.value = true
  try {
    const data = await get('/works/my')
    works.value = data || []

    for (const work of works.value) {
      if (work.developmentStatus === 'SUBMITTED') {
        try {
          const statusData = await get(`/ai-reviews/status/${work.id}`)
          work.aiReviewStatus = statusData
          if (statusData?.status === 'VALIDATING') {
            startPolling(work.id)
          }
        } catch (error) {
          console.error('获取AI评审状态失败', error)
        }

        // 获取评审员评审状态
        try {
          const submissionData = await get(`/submissions/work/${work.id}`)
          if (submissionData && submissionData.id) {
            // 使用正确的API获取JudgeReview列表
            const judgeReviews = await get(`/reviews/judge/submission/${submissionData.id}`)
            if (judgeReviews && judgeReviews.length > 0) {
              // 如果有评审记录，判断状态
              const hasDraft = judgeReviews.some(r => r.status === 'DRAFT')
              const hasSubmitted = judgeReviews.some(r => r.status === 'SUBMITTED')

              work.judgeReviewStatus = {
                status: hasSubmitted ? 'COMPLETED' : (hasDraft ? 'PENDING' : 'NOT_ASSIGNED'),
                totalScore: judgeReviews[0]?.overallScore || null,
                judgeName: judgeReviews[0]?.judgeName || null,
                judgeCount: judgeReviews.length
              }
            } else {
              // 没有评审记录，说明未分配
              work.judgeReviewStatus = {
                status: 'NOT_ASSIGNED',
                totalScore: null,
                judgeName: null,
                judgeCount: 0
              }
            }
          } else {
            // 没有提交记录，说明未提交
            work.judgeReviewStatus = {
              status: 'NOT_ASSIGNED',
              totalScore: null,
              judgeName: null,
              judgeCount: 0
            }
          }
        } catch (error) {
          // 如果获取失败，设置为待分配状态
          console.error('获取评审员评审状态失败', error)
          work.judgeReviewStatus = {
            status: 'NOT_ASSIGNED',
            totalScore: null,
            judgeName: null,
            judgeCount: 0
          }
        }
      }
    }
  } catch (error) {
    showError('获取作品列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTeams = async () => {
  try {
    const data = await get('/teams/my')
    teams.value = data || []
  } catch (error) {
    console.error('获取团队列表失败', error)
  }
}

const fetchWorkAttachments = async (workId) => {
  try {
    const data = await get(`/upload/work/${workId}`)
    workAttachments.value[workId] = data || []
  } catch (error) {
    console.error('获取附件列表失败', error)
  }
}

const getWorkAttachments = (workId) => {
  return workAttachments.value[workId] || []
}

const showCreateDialog = () => {
  createWorkForm.value = {
    workName: '',
    teamId: '',
    competitionId: 1,
    trackId: '',
    description: ''
  }
  showCreateWorkDialog.value = true
}

const handleTeamChange = async (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  if (team) {
    createWorkForm.value.trackId = team.competitionTrackId

    try {
      const registrations = await get(`/teams/${teamId}/registrations`)
      if (registrations && registrations.length > 0) {
        const approvedReg = registrations.find(r => r.status === 'APPROVED')
        if (approvedReg) {
          createWorkForm.value.competitionId = approvedReg.competitionId
        } else {
          createWorkForm.value.competitionId = registrations[0].competitionId
        }
      }
    } catch (error) {
      console.error('获取团队报名记录失败', error)
    }
  }
}

const handleCreateWork = async () => {
  if (!createWorkForm.value.workName) {
    showWarning('请输入作品名称')
    return
  }
  if (!createWorkForm.value.teamId) {
    showWarning('请选择团队')
    return
  }
  if (!createWorkForm.value.trackId) {
    showWarning('该团队未关联赛道，请确认团队已报名参赛')
    return
  }

  creating.value = true
  try {
    const queryParams = new URLSearchParams({
      workName: createWorkForm.value.workName,
      teamId: createWorkForm.value.teamId,
      competitionId: createWorkForm.value.competitionId,
      trackId: createWorkForm.value.trackId
    }).toString()

    await post(`/works?${queryParams}`)

    showSuccess('作品创建成功')
    showCreateWorkDialog.value = false
    await fetchWorks()
  } catch (error) {
    showError('创建作品失败')
  } finally {
    creating.value = false
  }
}

const editWork = (work) => {
  editWorkForm.value = {
    id: work.id,
    workName: work.workName,
    description: work.description || '',
    innovationPoints: work.innovationPoints || '',
    keyFeatures: work.keyFeatures || '',
    techStack: work.techStack || '',
    divisionOfLabor: work.divisionOfLabor || '',
    targetUsers: work.targetUsers || '',
    workType: work.workType
  }
  showEditWorkDialog.value = true
}

const handleUpdateWork = async () => {
  if (!editWorkForm.value.workName) {
    showWarning('请输入作品名称')
    return
  }

  updating.value = true
  try {
    const params = new URLSearchParams({
      workName: editWorkForm.value.workName,
      description: editWorkForm.value.description,
      innovationPoints: editWorkForm.value.innovationPoints,
      keyFeatures: editWorkForm.value.keyFeatures,
      techStack: editWorkForm.value.techStack,
      divisionOfLabor: editWorkForm.value.divisionOfLabor,
      targetUsers: editWorkForm.value.targetUsers
    })

    await put(`/works/${editWorkForm.value.id}?${params.toString()}`)
    showSuccess('作品信息已更新')
    showEditWorkDialog.value = false
    await fetchWorks()
  } catch (error) {
    showError('更新作品失败')
  } finally {
    updating.value = false
  }
}

const showUploadDialog = (work) => {
  uploadForm.value = {
    workId: work.id,
    file: null,
    attachmentType: 'SOURCE'
  }
  showUploadFileDialog.value = true
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    uploadForm.value.file = file
  }
}

const handleUploadFile = async () => {
  if (!uploadForm.value.file) {
    showWarning('请选择文件')
    return
  }

  uploading.value = true
  try {
    await uploadFile(
      `/upload/work/${uploadForm.value.workId}`,
      uploadForm.value.file,
      { attachmentType: uploadForm.value.attachmentType }
    )

    showSuccess('文件上传成功')
    showUploadFileDialog.value = false
    await fetchWorkAttachments(uploadForm.value.workId)
  } catch (error) {
    showError('上传失败')
  } finally {
    uploading.value = false
  }
}

const deleteAttachment = async (attachment) => {
  try {
    await showConfirm(`确定要删除文件 ${attachment.fileName} 吗？`, '删除文件')

    await del(`/upload/${attachment.id}`)
    showSuccess('文件已删除')
    await fetchWorkAttachments(attachment.workId)
  } catch (error) {
    if (error !== 'cancel') {
      showError('删除失败')
    }
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const markCompleted = async (work) => {
  try {
    await showConfirm('确定要将作品标记为已完成吗？标记后将可以提交评审。', '标记完成')

    await post(`/works/${work.id}/complete`)
    showSuccess('作品已标记为完成')
    await fetchWorks()
  } catch (error) {
    if (error !== 'cancel') {
      showError('标记完成失败')
    }
  }
}

const submitWork = async (work) => {
  console.log('submitWork called for work:', work.id, 'status:', work.developmentStatus)

  const timeStatus = timeStatusMap.value[work.competitionId]
  if (timeStatus && !timeStatus.canSubmit) {
    const endTime = formatDateTime(timeStatus.submissionEnd)
    showWarning(`作品提交已截止，截止时间：${endTime}`)
    return
  }

  try {
    const isResubmit = work.developmentStatus === 'SUBMITTED'
    const message = isResubmit
      ? '重新提交作品将更新评审信息，之前的评审任务将被取消。\n\n确定要重新提交吗？'
      : '提交作品后将进入评审流程，无法撤回。\n\n确定要提交吗？'

    console.log('Showing confirm dialog with message:', message)

    // 使用 ElMessageBox.confirm 直接调用，确保对话框显示
    await ElMessageBox.confirm(
      message,
      isResubmit ? '重新提交作品' : '提交作品',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )

    console.log('User confirmed submission, calling API...')
    showInfo('正在提交作品...')

    await post(`/works/${work.id}/submit`)
    console.log('API call successful')

    showSuccess(isResubmit ? '✅ 作品已重新提交，AI正在评审中' : '✅ 作品已提交，AI正在评审中')
    await fetchWorks()
  } catch (error) {
    console.error('submitWork error:', error)
    if (error === 'cancel') {
      showInfo('已取消提交')
    } else {
      showError(`提交失败：${error.message || '未知错误'}`)
    }
  }
}

const getStatusClass = (status) => {
  return {
    'badge-primary': status === 'SUBMITTED' || status === 'AWARDED',
    'badge-warning': status === 'IN_PROGRESS',
    'badge-gray': status === 'COMPLETED'
  }
}

const getStatusText = (status) => {
  const texts = {
    IN_PROGRESS: '开发中',
    COMPLETED: '已完成',
    SUBMITTED: '已提交',
    AWARDED: '已获奖'
  }
  return texts[status] || status
}

const getWorkTypeText = (type) => {
  const texts = {
    CODE: '程序设计',
    PPT: '演示文稿',
    VIDEO: '数媒动漫'
  }
  return texts[type] || type
}

const formatDate = (dateStr) => {
  return formatDateTime(dateStr)
}

const getSubmissionCountdown = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return ''

  if (timeStatus.currentPhase === 'SUBMISSION' && timeStatus.submissionDaysRemaining) {
    return `提交剩余 ${timeStatus.submissionDaysRemaining} 天`
  }
  return ''
}

const isSubmissionClosed = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return false
  return timeStatus.currentPhase === 'FINISHED' ||
         (timeStatus.currentPhase !== 'BEFORE_SUBMISSION' &&
          timeStatus.currentPhase !== 'SUBMISSION' &&
          !timeStatus.canSubmit)
}

const isSubmissionNotStarted = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return false
  return timeStatus.currentPhase === 'BEFORE_SUBMISSION'
}

const getSubmissionStartTime = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return ''
  return formatDateTime(timeStatus.submissionStart)
}

const getAIStatusClass = (status) => {
  return {
    'badge-warning': status === 'VALIDATING',
    'badge-primary': status === 'VALID',
    'badge-danger': status === 'INVALID'
  }
}

const getAIStatusText = (status) => {
  const texts = {
    VALIDATING: 'AI评审中',
    VALID: '评审通过',
    INVALID: '评审未通过'
  }
  return texts[status] || status
}

const getJudgeStatusClass = (status) => {
  return {
    'badge-warning': status === 'PENDING',
    'badge-primary': status === 'COMPLETED',
    'badge-gray': status === 'NOT_ASSIGNED'
  }
}

const getJudgeStatusText = (status) => {
  const texts = {
    PENDING: '评审中',
    COMPLETED: '评审完成',
    NOT_ASSIGNED: '待分配'
  }
  return texts[status] || status
}

const getJudgeScoreClass = (score) => {
  if (score >= 90) return 'badge-primary'
  if (score >= 80) return 'badge-success'
  if (score >= 70) return 'badge-warning'
  return 'badge-danger'
}

const getJudgeScoreText = (score) => {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  return '需改进'
}

const viewJudgeReport = async (work) => {
  try {
    showInfo('正在加载评审员评审报告...')

    const submissionData = await get(`/submissions/work/${work.id}`)
    if (!submissionData || !submissionData.id) {
      showError('作品尚未提交，无法查看评审报告')
      return
    }

    // 获取评审员评审列表
    const judgeReviews = await get(`/reviews/judge/submission/${submissionData.id}`)
    if (judgeReviews && judgeReviews.length > 0) {
      // 找到已提交的评审记录
      const submittedReview = judgeReviews.find(r => r.status === 'SUBMITTED') || judgeReviews[0]

      currentJudgeReport.value = {
        totalScore: submittedReview.overallScore || 0,
        judgeName: submittedReview.judgeName || '未分配',
        reviewTime: submittedReview.reviewTime,
        comments: submittedReview.reviewComment || '暂无评审意见',
        strengths: [],
        weaknesses: [],
        scoreItems: [
          { criteriaName: '创新性', score: submittedReview.innovationScore || 0, maxScore: 25 },
          { criteriaName: '实用性', score: submittedReview.practicalityScore || 0, maxScore: 25 },
          { criteriaName: '用户体验', score: submittedReview.userExperienceScore || 0, maxScore: 25 }
        ],
        judgeCount: judgeReviews.length
      }
      showJudgeReportDialog.value = true
    } else {
      showWarning('暂无评审员评审报告')
    }
  } catch (error) {
    showError('获取评审员评审报告失败')
  }
}

// 评分环形进度条样式
const getScoreRingStyle = (score) => {
  const percentage = score ? (score / 100 * 360) : 0
  const color = score >= 90 ? '#4CAF50' : score >= 80 ? '#2196F3' : score >= 70 ? '#FF9800' : '#F44336'
  return {
    background: `conic-gradient(${color} ${percentage}deg, #E0E0E0 ${percentage}deg)`
  }
}

const getRiskClass = (level) => {
  return {
    'badge-primary': level === 'LOW',
    'badge-warning': level === 'MEDIUM',
    'badge-danger': level === 'HIGH'
  }
}

const getRiskText = (level) => {
  const texts = {
    LOW: '低',
    MEDIUM: '中等',
    HIGH: '高'
  }
  return texts[level] || level
}

const getDuplicateClass = (rate) => {
  if (rate > 30) return 'metric-danger'
  if (rate > 20) return 'metric-warning'
  return 'metric-good'
}

const getScoreGradient = (score, status) => {
  // 如果评审未通过且没有分数，使用深红色背景
  if (status === 'INVALID' && !score) {
    return 'background: linear-gradient(135deg, #C62828, #B71C1C)'
  }
  if (!score) return 'background: linear-gradient(135deg, #E0E0E0, #BDBDBD)'
  if (score >= 90) return 'background: linear-gradient(135deg, #FFD700, #FFA500)'
  if (score >= 80) return 'background: linear-gradient(135deg, #4CAF50, #66BB6A)'
  if (score >= 70) return 'background: linear-gradient(135deg, #2196F3, #42A5F5)'
  if (score >= 60) return 'background: linear-gradient(135deg, #FF9800, #FFB74D)'
  return 'background: linear-gradient(135deg, #F44336, #EF5350)'
}

const getProgressStageClass = (status, stage, aiReviewStatus) => {
  const statusMap = {
    'IN_PROGRESS': 1,
    'COMPLETED': 2,
    'SUBMITTED': 3,
    'AWARDED': 4
  }
  const currentStage = statusMap[status] || 0

  if (stage < currentStage) return 'completed'
  if (stage === currentStage) {
    // 检查是否在评审中（SUBMITTED状态且AI评审状态为VALIDATING）
    if (status === 'SUBMITTED' && aiReviewStatus?.status === 'VALIDATING') {
      return 'active reviewing'
    }
    return 'active'
  }
  return ''
}

const getProgressLineClass = (status, line) => {
  const statusMap = {
    'IN_PROGRESS': 1,
    'COMPLETED': 2,
    'SUBMITTED': 3,
    'AWARDED': 4
  }
  const currentStage = statusMap[status] || 0

  if (line < currentStage) return 'completed'
  return ''
}

const getTypeIcon = (type) => {
  const icons = {
    'CODE': '< />',
    'PPT': '📊',
    'VIDEO': '🎬'
  }
  return icons[type] || '?'
}

const shouldShowTimeBanner = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return false
  return timeStatus.currentPhase === 'SUBMISSION' && timeStatus.submissionDaysRemaining !== undefined
}

const getTimeBannerLevel = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return 'normal'

  const daysRemaining = timeStatus.submissionDaysRemaining
  if (daysRemaining < 3) return 'closed'
  if (daysRemaining <= 7) return 'urgent'
  return 'normal'
}

const getTimeBannerMessage = (work) => {
  const timeStatus = timeStatusMap.value[work.competitionId]
  if (!timeStatus) return ''

  const daysRemaining = timeStatus.submissionDaysRemaining
  if (daysRemaining < 3) return `⏰ 截止日期临近，仅剩 ${daysRemaining} 天`
  if (daysRemaining <= 7) return `⚠️ 仅剩 ${daysRemaining} 天，请尽快提交`
  return `剩余 ${daysRemaining} 天`
}

const startPolling = (workId) => {
  const token = localStorage.getItem('token')
  if (!token) {
    console.warn('User not logged in, skipping polling')
    return
  }

  if (!workId || workId <= 0) {
    console.warn('Invalid workId, skipping polling')
    return
  }

  if (pollingIntervals.value[workId]) return

  const pollInterval = setInterval(async () => {
    try {
      const statusData = await get(`/ai-reviews/status/${workId}`)

      const work = works.value.find(w => w.id === workId)
      if (work) {
        work.aiReviewStatus = statusData
      }

      if (statusData?.status !== 'VALIDATING') {
        clearInterval(pollInterval)
        pollingIntervals.value[workId] = null

        if (statusData?.status === 'VALID') {
          showSuccess('AI评审已完成，作品评审通过！')
        } else if (statusData?.status === 'INVALID') {
          showWarning('AI评审已完成，作品评审未通过，请查看报告。')
        }
      }
    } catch (error) {
      console.error('轮询AI评审状态失败', error)
      if (error.message && error.message.includes('403')) {
        clearInterval(pollInterval)
        pollingIntervals.value[workId] = null
      }
    }
  }, 5000)

  pollingIntervals.value[workId] = pollInterval

  setTimeout(() => {
    if (pollingIntervals.value[workId]) {
      clearInterval(pollingIntervals.value[workId])
      pollingIntervals.value[workId] = null
    }
  }, 600000)
}

const refreshAIStatus = async (work) => {
  try {
    showInfo('正在刷新AI评审状态...')
    const statusData = await get(`/ai-reviews/status/${work.id}`)
    work.aiReviewStatus = statusData

    // 如果状态不再是VALIDATING，停止轮询
    if (statusData?.status !== 'VALIDATING') {
      if (pollingIntervals.value[work.id]) {
        clearInterval(pollingIntervals.value[work.id])
        pollingIntervals.value[work.id] = null
      }

      if (statusData?.status === 'VALID') {
        showSuccess('AI评审已完成，作品评审通过！')
      } else if (statusData?.status === 'INVALID') {
        showWarning('AI评审已完成，作品评审未通过。')
      }
    } else {
      showInfo('AI评审仍在进行中...')
    }
  } catch (error) {
    showError('刷新状态失败')
  }
}

const viewAIReport = async (work) => {
  try {
    const submissionData = await get(`/submissions/work/${work.id}`)
    if (!submissionData || !submissionData.id) {
      showError('作品尚未提交，无法查看评审报告')
      return
    }

    const detailDTO = await get(`/ai-reviews/detail/${submissionData.id}`)
    currentAIReport.value = detailDTO
    showAIReportDialog.value = true
  } catch (error) {
    showError('获取AI评审报告失败')
  }
}
</script>

<style scoped>
.works-page {
  min-height: 100vh;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.action-bar {
  animation-delay: 0.1s;
}

.works-section {
  animation-delay: 0.2s;
}

.loading-state {
  padding: var(--spacing-3xl) 0;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.work-card {
  padding: var(--spacing-xl);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.countdown-section {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(90, 127, 168, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.countdown-text {
  font-weight: 600;
  font-size: 14px;
}

.deadline-warning {
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(231, 76, 60, 0.1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.warning-text {
  color: #e74c3c;
  font-weight: 600;
  font-size: 14px;
}

.work-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-label {
  color: var(--color-text-secondary);
}

.info-value {
  font-weight: 500;
}

.work-description {
  color: var(--color-text-secondary);
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.empty-state {
  padding: var(--spacing-3xl);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--spacing-lg);
  opacity: 0.5;
}

.work-attachments {
  background: var(--color-bg-secondary);
  padding: var(--spacing-lg);
  border-radius: var(--radius-md);
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}

.attachment-name {
  flex: 1;
}

.attachment-size {
  color: var(--color-text-secondary);
}

.btn-text {
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.btn-delete {
  color: #e74c3c;
}

.btn-delete:hover {
  color: #c0392b;
}

.file-input {
  width: 100%;
  padding: var(--spacing-md);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.selected-file {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
  margin-top: var(--spacing-md);
}

.file-size {
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .work-info-grid {
    grid-template-columns: 1fr;
  }

  .attachment-item {
    flex-wrap: wrap;
  }
}

/* AI Review Status Styles */

.ai-review-status {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.status-label {
  color: var(--color-text-secondary);
}

.review-progress {
  margin-top: var(--spacing-md);
}

.progress-bar {
  height: 8px;
  background: var(--color-border-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--color-accent);
  width: 60%;
  animation: progress-animation 2s ease-in-out infinite;
}

@keyframes progress-animation {
  0% { width: 30%; }
  50% { width: 70%; }
  100% { width: 30%; }
}

.progress-text {
  display: block;
  margin-top: var(--spacing-sm);
  color: var(--color-text-secondary);
}

.review-result {
  margin-top: var(--spacing-md);
}

.score-display {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.score-value {
  font-size: var(--text-2xl);
  font-weight: 700;
}

.score-pass {
  color: var(--status-completed);
}

.score-fail {
  color: var(--status-danger);
}

.risk-info {
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.badge-danger {
  background: #e74c3c;
  color: white;
  font-weight: 600;
}

.badge-gray {
  background: #9E9E9E;
  color: white;
  font-weight: 600;
}

.judge-count-badge {
  background: rgba(52, 152, 219, 0.15);
  color: var(--color-accent);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
}

.badge-lg {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--text-sm);
}

/* AI Review Report Dialog Styles */

.ai-report-content {
  padding: var(--spacing-md);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.main-score {
  display: flex;
  align-items: baseline;
}

.score-number {
  font-size: 48px;
  font-weight: 700;
  color: var(--color-accent);
}

.score-unit {
  font-size: var(--text-lg);
  color: var(--color-text-secondary);
  margin-left: var(--spacing-xs);
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.score-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.item-label {
  width: 80px;
  color: var(--color-text-secondary);
}

.score-bar {
  flex: 1;
  height: 12px;
  background: var(--color-border-light);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.score-bar-fill {
  height: 100%;
  background: var(--color-accent);
  border-radius: var(--radius-sm);
  transition: width 0.3s ease;
}

.item-value {
  width: 50px;
  text-align: right;
  font-weight: 600;
}

.code-quality {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.quality-metrics {
  display: flex;
  gap: var(--spacing-xl);
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.metric-label {
  color: var(--color-text-secondary);
}

.metric-value {
  font-weight: 600;
  font-size: var(--text-lg);
}

.metric-good {
  color: var(--status-completed);
}

.metric-warning {
  color: var(--status-pending);
}

.metric-danger {
  color: var(--status-danger);
}

.review-summary-section,
.suggestions-section {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-primary);
}

.suggestions-list {
  list-style: disc;
  padding-left: var(--spacing-lg);
}

/* Judge Review Report Styles */

.judge-report-content {
  padding: var(--spacing-md);
}

.judge-info {
  display: flex;
  gap: var(--spacing-xl);
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

.judge-info .info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.badge-success {
  background: #4CAF50;
  color: white;
}

.review-comments-section,
.strengths-section,
.weaknesses-section {
  padding: var(--spacing-md);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
}

/* Dashboard-Style Works Layout */

.works-dashboard {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.work-dashboard-card {
  display: flex;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: visible;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 280px;
}

.work-dashboard-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

/* Left AI Score Bar */

.ai-score-bar {
  width: 80px;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  flex-shrink: 0;
}

.ai-score-bar.reviewing {
  background: linear-gradient(135deg, #9E9E9E, #757575);
}

.ai-score-bar.invalid {
  background: linear-gradient(135deg, #C62828, #B71C1C);
}

.ai-score-bar.invalid .score-value,
.ai-score-bar.invalid .score-label {
  color: white;
}

.review-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.review-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: review-spin 1s linear infinite;
}

@keyframes review-spin {
  to { transform: rotate(360deg); }
}

.review-text {
  font-size: 13px;
  color: white;
  font-weight: 500;
  animation: pulse-text 2s ease-in-out infinite;
}

@keyframes pulse-text {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.ai-score-bar .score-value {
  font-size: 36px;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.ai-score-bar .score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: var(--spacing-sm);
}

/* Main Content Area */

.work-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--spacing-lg);
  gap: var(--spacing-md);
}

/* Progress Flow Bar */

.progress-flow {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
}

.flow-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}

.stage-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #E0E0E0;
  transition: background 0.3s ease;
}

.flow-stage.completed .stage-dot {
  background: #4CAF50;
}

.flow-stage.active .stage-dot {
  background: var(--color-accent);
  animation: pulse 2s ease-in-out infinite;
}

.flow-stage.reviewing .stage-dot {
  background: linear-gradient(135deg, #FF9800, #FFB74D);
  animation: review-pulse 1.5s ease-in-out infinite;
}

@keyframes review-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 152, 0, 0.7);
  }
  50% {
    transform: scale(1.2);
    box-shadow: 0 0 0 10px rgba(255, 152, 0, 0);
  }
}

.flow-stage.reviewing .stage-label {
  color: #FF9800;
  font-weight: 600;
  animation: blink 1.5s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(0, 113, 227, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(0, 113, 227, 0);
  }
}

.stage-label {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.flow-stage.completed .stage-label,
.flow-stage.active .stage-label {
  color: var(--color-primary);
  font-weight: 500;
}

.flow-line {
  width: 40px;
  height: 2px;
  background: #E0E0E0;
}

.flow-line.completed {
  background: #4CAF50;
}

/* Work Details */

.work-details {
  flex: 1;
}

.work-title-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.work-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-primary);
  flex: 1;
}

.type-badge {
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.type-badge.type-CODE {
  background: linear-gradient(135deg, #7B68EE, #6A5ACD);
}

.type-badge.type-PPT {
  background: linear-gradient(135deg, #FF69B4, #DA70D6);
}

.type-badge.type-VIDEO {
  background: linear-gradient(135deg, #00CED1, #20B2AA);
}

.work-meta {
  display: flex;
  gap: var(--spacing-lg);
  margin-top: var(--spacing-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  font-size: 13px;
}

/* AI Status Mini */

.review-status-section {
  margin-top: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.review-type-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
}

.review-type-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.status-mini-badge {
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
}

.badge-warning {
  background: linear-gradient(135deg, #FF9800, #FFB74D);
  color: white;
  animation: badge-pulse 2s ease-in-out infinite;
}

@keyframes badge-pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 152, 0, 0.5);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(255, 152, 0, 0);
  }
}

.view-report-btn,
.refresh-btn {
  padding: 4px 8px;
  border: 1px solid var(--color-border);
  background: transparent;
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-report-btn:hover,
.refresh-btn:hover {
  background: var(--color-bg-secondary);
  color: var(--color-primary);
}

/* Attachments Mini */

.attachments-mini {
  margin-top: var(--spacing-md);
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-sm);
}

.attachments-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-secondary);
}

.attachments-label {
  font-size: 12px;
  font-weight: 500;
}

.attachments-count {
  background: var(--color-accent);
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.attachments-items {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.attachment-item-mini {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 4px var(--spacing-sm);
  background: var(--color-bg);
  border-radius: var(--radius-xs);
  font-size: 12px;
}

.attachment-item-mini .attachment-name {
  flex: 1;
  color: var(--color-text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-item-mini .attachment-size {
  color: var(--color-text-secondary);
  font-size: 11px;
}

.delete-attachment-btn {
  display: flex;
  align-items: center;
  padding: 2px;
  background: transparent;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.2s ease;
}

.delete-attachment-btn:hover {
  color: #e74c3c;
}

/* Time Warning Banner */

.time-banner {
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  text-align: center;
}

.time-banner.normal {
  background: #F5F5F7;
  color: var(--color-text-primary);
}

.time-banner.urgent {
  background: linear-gradient(135deg, #FFB74D, #FFA000);
  color: white;
}

.time-banner.closed {
  background: linear-gradient(135deg, #EF5350, #C62828);
  color: white;
}

/* Work Actions Bar */

.work-actions-bar {
  display: flex;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: var(--color-accent);
  color: white;
  border-color: var(--color-accent);
}

.action-btn-complete {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
  border-color: #4CAF50;
}

.action-btn-submit {
  background: linear-gradient(135deg, #0071e3, #42A5F5);
  color: white;
  border-color: #0071e3;
}

.action-btn-resubmit {
  background: linear-gradient(135deg, #FF9800, #FFB74D);
  color: white;
  border-color: #FF9800;
}

/* Award Trophy Icon */

.award-trophy {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  animation: rotate-trophy 3s ease-in-out infinite;
}

@keyframes rotate-trophy {
  0%, 100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(15deg);
  }
}

/* Awarded Works Special Styling */

.work-dashboard-card.awarded {
  background: linear-gradient(135deg, #FFD700, #FFC107);
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.6);
  animation: glow-pulse 2s infinite;
}

@keyframes glow-pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(255, 215, 0, 0.4);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 215, 0, 0.8);
  }
}

/* Type-Specific Border Accents */

.work-dashboard-card[data-type="CODE"] {
  border-left: 4px solid #7B68EE;
}

.work-dashboard-card[data-type="PPT"] {
  border-left: 4px solid #FF69B4;
}

.work-dashboard-card[data-type="VIDEO"] {
  border-left: 4px solid #00CED1;
}

/* Responsive Design */

@media (max-width: 768px) {
  .work-dashboard-card {
    height: auto;
    flex-direction: column;
  }

  .ai-score-bar {
    width: 100%;
    border-radius: var(--radius-lg) var(--radius-lg) 0 0;
    padding: var(--spacing-md);
  }

  .ai-score-bar .score-value {
    font-size: 28px;
  }

  .progress-flow {
    flex-wrap: wrap;
    justify-content: center;
  }

  .flow-line {
    width: 20px;
  }

  .work-actions-bar {
    flex-wrap: wrap;
  }
}

/* AI Review Report Beautified Styles */
.ai-report-beautified {
  padding: 32px;
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F7 100%);
  border-radius: 20px;
}

.report-score-hero {
  display: flex;
  gap: 40px;
  padding: 32px;
  background: linear-gradient(135deg, rgba(0, 113, 227, 0.03) 0%, rgba(0, 113, 227, 0.06) 100%);
  border-radius: 16px;
  margin-bottom: 24px;
}

.score-circle-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.score-ring-big {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 8px 24px rgba(0, 113, 227, 0.2);
  transition: all 0.5s ease;
}

.score-ring-big::before {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  background: white;
  border-radius: 50%;
  z-index: 1;
}

.score-center {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.big-score-number {
  font-size: 44px;
  font-weight: 800;
  color: var(--color-text-primary);
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.score-label-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.dimension-bars {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dimension-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dim-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 90px;
}

.dim-emoji {
  font-size: 18px;
}

.dim-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-secondary);
}

.dim-bar-wrapper {
  flex: 1;
  height: 20px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  overflow: hidden;
}

.dim-bar-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.dim-bar-fill.innovation {
  background: linear-gradient(90deg, #FFD700, #FFA500);
}

.dim-bar-fill.practicality {
  background: linear-gradient(90deg, #2196F3, #42A5F5);
}

.dim-bar-fill.experience {
  background: linear-gradient(90deg, #FF69B4, #DA70D6);
}

.dim-bar-fill.documentation {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
}

/* PPT作品评分维度样式 */
.dim-bar-fill.creativity {
  background: linear-gradient(90deg, #FF69B4, #DA70D6);
}

.dim-bar-fill.visual {
  background: linear-gradient(90deg, #00CED1, #20B2AA);
}

.dim-bar-fill.content {
  background: linear-gradient(90deg, #7B68EE, #6A5ACD);
}

.dim-bar-fill.originality {
  background: linear-gradient(90deg, #FFD700, #FFA500);
}

/* VIDEO作品评分维度样式 */
.dim-bar-fill.story {
  background: linear-gradient(90deg, #4CAF50, #66BB6A);
}

.dim-bar-fill.director {
  background: linear-gradient(90deg, #FF9800, #FFB74D);
}

.dim-score {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
  min-width: 60px;
  text-align: right;
}

/* Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.metric-box {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.metric-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.metric-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1), rgba(255, 152, 0, 0.15));
  color: #FF9800;
}

.complex-icon {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(76, 175, 80, 0.15));
  color: #4CAF50;
}

.duplicate-icon {
  background: linear-gradient(135deg, rgba(244, 67, 54, 0.1), rgba(244, 67, 54, 0.15));
  color: #F44336;
}

.metric-content {
  flex: 1;
}

.metric-title {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.metric-display {
  font-size: 18px;
  font-weight: 700;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
}

.risk-LOW {
  color: #4CAF50;
}

.risk-MEDIUM {
  color: #FF9800;
}

.risk-HIGH {
  color: #F44336;
}

.complex-LOW {
  color: #4CAF50;
}

.complex-MEDIUM {
  color: #FF9800;
}

.complex-HIGH {
  color: #F44336;
}

.duplicate-value {
  color: #F44336;
  font-size: 16px;
}

/* Summary Box */
.summary-box {
  padding: 24px;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  border-left: 4px solid #0071e3;
  transition: all 0.3s ease;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #0071e3;
}

.summary-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.summary-body p {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-primary);
  margin: 0;
}

/* Suggestions Box */
.suggestions-box {
  padding: 24px;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.03), white);
  border-radius: 12px;
  border-left: 4px solid #4CAF50;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: #4CAF50;
}

.suggestions-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.suggestion-item:hover {
  transform: translateX(4px);
  background: rgba(76, 175, 80, 0.05);
}

.suggestion-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.suggestion-text {
  flex: 1;
  font-size: 15px;
  line-height: 1.6;
  color: var(--color-text-primary);
  font-weight: 500;
}

.report-close-btn {
  background: linear-gradient(135deg, #0071e3, #0077ED);
  color: white;
  border: none;
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.report-close-btn:hover {
  background: linear-gradient(135deg, #0077ED, #0071e3);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 113, 227, 0.3);
}
</style>