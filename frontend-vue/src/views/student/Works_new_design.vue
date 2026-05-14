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
              :style="getScoreGradient(work.aiReviewStatus?.overallScore)"
            >
              <div class="score-value">
                {{ work.aiReviewStatus?.overallScore || '--' }}
              </div>
              <div class="score-label">AI评分</div>
            </div>

            <!-- Main Content Area -->
            <div class="work-main">
              <!-- Top Progress Flow Bar -->
              <div class="progress-flow">
                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 1)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">制作中</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 1)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 2)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">已完成</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 2)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 3)">
                  <div class="stage-dot"></div>
                  <span class="stage-label">已提交</span>
                </div>
                <div class="flow-line" :class="getProgressLineClass(work.developmentStatus, 3)"></div>

                <div class="flow-stage" :class="getProgressStageClass(work.developmentStatus, 4)">
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
                <div v-if="work.aiReviewStatus" class="ai-status-mini">
                  <span :class="getAIStatusClass(work.aiReviewStatus.status)" class="status-mini-badge">
                    {{ getAIStatusText(work.aiReviewStatus.status) }}
                  </span>
                  <button
                    v-if="work.aiReviewStatus.status !== 'PENDING'"
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

            <!-- Award Trophy Icon (for awarded works) -->
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
          <el-form-item label="所属团队" required>
            <el-select
              v-model="createWorkForm.teamId"
              placeholder="请选择团队"
              @change="handleTeamChange"
              style="width: 100%"
            >
              <el-option
                v-for="team in teams"
                :key="team.id"
                :label="team.teamName"
                :value="team.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="作品描述">
            <el-input
              v-model="createWorkForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入作品描述（可选）"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showCreateWorkDialog = false">取消</el-button>
            <el-button type="primary" @click="handleCreateWork" :loading="createLoading">
              创建作品
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- Upload File Dialog -->
      <el-dialog
        v-model="showUploadDialogVisible"
        title="上传作品文件"
        width="600px"
      >
        <el-form label-width="100px">
          <el-form-item label="作品">
            <span>{{ currentWork?.workName }}</span>
          </el-form-item>
          <el-form-item label="上传文件">
            <el-upload
              ref="uploadRef"
              :action="`http://localhost:8080/api/upload/work/${currentWork?.id}`"
              :headers="{ Authorization: `Bearer ${localStorage.getItem('token')}` }"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :limit="1"
              :auto-upload="false"
            >
              <template #trigger>
                <el-button type="primary">选择文件</el-button>
              </template>
              <el-button type="success" @click="submitUpload">
                上传到服务器
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .zip, .rar, .mp4, .pptx 等格式，文件大小不超过 300MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!-- AI Review Report Dialog -->
      <el-dialog
        v-model="showAIReportDialog"
        title="AI审核报告"
        width="800px"
      >
        <div v-if="aiReportLoading" class="loading-state">
          <div class="spinner"></div>
        </div>
        <div v-else-if="aiReport" class="ai-report-content">
          <!-- Overall Score -->
          <div class="report-header">
            <div class="overall-score-section">
              <div class="overall-score-display" :class="aiReport.overallScore >= 60 ? 'score-pass' : 'score-fail'">
                <div class="overall-score-value">{{ aiReport.overallScore }}</div>
                <div class="overall-score-label">综合得分</div>
              </div>
              <div class="risk-badge" :class="'risk-' + aiReport.riskLevel">
                {{ getRiskText(aiReport.riskLevel) }}风险
              </div>
            </div>
          </div>

          <!-- Score Dimensions -->
          <div class="score-dimensions">
            <div class="dimension-item">
              <div class="dimension-label">创新性</div>
              <div class="dimension-score">{{ aiReport.innovationScore || 0 }}</div>
            </div>
            <div class="dimension-item">
              <div class="dimension-label">实用性</div>
              <div class="dimension-score">{{ aiReport.practicalityScore || 0 }}</div>
            </div>
            <div class="dimension-item">
              <div class="dimension-label">用户体验</div>
              <div class="dimension-score">{{ aiReport.userExperienceScore || 0 }}</div>
            </div>
            <div class="dimension-item">
              <div class="dimension-label">文档质量</div>
              <div class="dimension-score">{{ aiReport.documentationScore || 0 }}</div>
            </div>
            <div v-if="aiReport.codeQualityScore" class="dimension-item">
              <div class="dimension-label">代码质量</div>
              <div class="dimension-score">{{ aiReport.codeQualityScore }}</div>
            </div>
            <div v-if="aiReport.duplicateRate" class="dimension-item">
              <div class="dimension-label">重复率</div>
              <div class="dimension-score duplicate">{{ aiReport.duplicateRate }}%</div>
            </div>
          </div>

          <!-- Review Summary -->
          <div class="report-section">
            <h4 class="section-title">评审总结</h4>
            <p class="review-summary">{{ aiReport.reviewSummary }}</p>
          </div>

          <!-- Strengths -->
          <div v-if="aiReport.strengths && aiReport.strengths.length > 0" class="report-section">
            <h4 class="section-title">作品亮点</h4>
            <ul class="strengths-list">
              <li v-for="(strength, index) in aiReport.strengths" :key="index">{{ strength }}</li>
            </ul>
          </div>

          <!-- Weaknesses -->
          <div v-if="aiReport.weaknesses && aiReport.weaknesses.length > 0" class="report-section">
            <h4 class="section-title">不足之处</h4>
            <ul class="weaknesses-list">
              <li v-for="(weakness, index) in aiReport.weaknesses" :key="index">{{ weakness }}</li>
            </ul>
          </div>

          <!-- Suggestions -->
          <div v-if="aiReport.improvementSuggestions && aiReport.improvementSuggestions.length > 0" class="report-section">
            <h4 class="section-title">改进建议</h4>
            <ul class="suggestions-list">
              <li v-for="(suggestion, index) in aiReport.improvementSuggestions" :key="index">{{ suggestion }}</li>
            </ul>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>