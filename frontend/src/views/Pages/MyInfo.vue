<template>
  <AdminLayout>
    <PageBreadcrumb pageTitle="내 정보 수정" />

    <div
        class="relative overflow-x-auto overflow-y-visible rounded-2xl border border-gray-200 bg-white p-0
             dark:border-gray-800 dark:bg-white/[0.03]"
    >
      <!-- 장식 그리드 -->
      <div
          class="pointer-events-none absolute inset-y-0 left-1/2 -translate-x-1/2 w-[100%] max-w-none select-none"
          aria-hidden="true"
      >
        <CommonGridShape
            src="/images/shape/grid-01.svg"
            mode="mask"
            size="w-56 h-56"
            position="-top-1 -right-4"
            opacity="opacity-[0.06] dark:opacity-[0.10]"
            lightColor="bg-gray-900"
            darkColor="dark:bg-gray-900"
        />
      </div>

      <!-- 가운데 정렬 컨테이너 -->
      <div class="relative z-10 mx-auto my-10 w-full max-w-[min(48rem,calc(100%-48px))]">
        <!-- 헤더 -->
        <div class="min-w-0 pt-5 lg:pt-6 mb-15">
          <h2 class="truncate text-2xl font-semibold text-gray-800 dark:text-white/90">
            {{ name || '사용자' }}
          </h2>
          <div class="mt-1 flex flex-wrap items-center gap-x-4 gap-y-1 text-m text-gray-500 dark:text-gray-400">
            <span>구분: <b class="text-gray-700 dark:text-gray-300">{{ roleLabel }}</b></span>
            <span>소속: <b class="text-gray-700 dark:text-gray-300">{{ orgLabel }}</b></span>
          </div>
<!--          <span v-if="!isVerified" class="text-gray-500 dark:text-gray-400 text-sm" >이메일 인증 후 내 정보 수정이 가능합니다.</span>-->
        </div>

        <!-- 공통 2열 폼 그리드 -->
        <div class="form-grid grid grid-cols-[6rem,1fr] items-start gap-x-10 gap-y-3 mx-2">

          <!-- 이메일 -->
<!--          <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">이메일</div>-->
<!--          <div class="col-start-2">-->
<!--            <div class="flex items-center gap-3">-->
<!--              <span-->
<!--                  class="truncate flex-1 min-w-[12rem] text-gray-800 dark:text-gray-300 ml-2"-->
<!--                  :title="email || '-'"-->
<!--              >-->
<!--                {{ email || '-' }}-->
<!--              </span>-->
<!--              <button-->
<!--                  type="button"-->
<!--                  class="h-11 px-4 rounded-lg text-sm font-medium text-white transition-->
<!--               bg-brand-500 hover:bg-brand-600 disabled:opacity-50 ml-auto"-->
<!--                  :disabled="sendingCode || isVerified"-->
<!--                  @click="sendVerify"-->
<!--              >-->
<!--                {{ isVerified ? '인증완료' : (sendingCode ? '전송 중...' : (codeSent ? '&nbsp;&nbsp;재요청&nbsp;&nbsp;' : '인증요청')) }}-->
<!--              </button>-->
<!--            </div>-->
<!--          </div>-->

          <!-- (2번째 화면) 인증코드 입력: codeSent && !isVerified -->
<!--          <template v-if="codeSent && !isVerified">-->
<!--            <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">인증번호</div>-->
<!--            <div class="col-start-2">-->
<!--              <div class="flex items-center gap-2 flex-wrap sm:flex-nowrap">-->
<!--                <input-->
<!--                    v-model="verifyCode"-->
<!--                    placeholder="메일로 받은 6자리"-->
<!--                    class="h-11 flex-1 min-w-[12rem] rounded-lg border px-3-->
<!--                 bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10-->
<!--                 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"-->
<!--                />-->
<!--                <button-->
<!--                    type="button"-->
<!--                    class="h-11 px-4 rounded-lg text-sm font-medium-->
<!--                 bg-gray-100 hover:bg-gray-200 text-gray-700-->
<!--                 dark:bg-white/5 dark:text-white/90 dark:hover:bg-white/10"-->
<!--                    :disabled="verifying"-->
<!--                    @click="verifyNow"-->
<!--                >-->
<!--                  {{ verifying ? '확인 중...' : '인증하기' }}-->
<!--                </button>-->
<!--              </div>-->
<!--            </div>-->

<!--            <div class="text-sm font-medium text-gray-700 dark:text-gray-300">남은시간</div>-->
<!--            <div class="col-start-2">-->
<!--              <div class="flex items-center justify-between">-->
<!--                <p class="text-sm text-blue-600">{{ mm }}:{{ ss }}</p>-->
<!--                <button-->
<!--                    type="button"-->
<!--                    class="px-3 py-2 rounded-lg text-sm-->
<!--                 text-gray-700 dark:text-gray-300-->
<!--                 hover:text-brand-500 disabled:opacity-50"-->
<!--                    :disabled="extendedOnce"-->
<!--                    @click="extendTime"-->
<!--                >-->
<!--                  &nbsp;&nbsp;1회 연장&nbsp;&nbsp;-->
<!--                </button>-->
<!--              </div>-->
<!--            </div>-->
<!--          </template>-->

          <!-- (3번째 화면) 인증 완료 후: isVerified -->
<!--          <template>-->

            <!-- 전화번호 -->
            <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">전화번호</div>
            <div class="col-start-2">
              <div class="flex gap-3">
                <input
                    v-model="phoneInput"
                    :disabled="savingPhone"
                    placeholder="010-1234-5678"
                    @input="formatPhoneInput"
                    @blur="validatePhoneInput"
                    class="h-11 w-full rounded-lg border px-3
                 bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                />
                <button
                    type="button"
                    class="h-11 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                    :disabled="savingPhone || phoneInput === phone"
                    @click="savePhone"
                >
                  {{ savingPhone ? '저장 중...' : '저장' }}
                </button>
              </div>
              <p v-if="phoneError" class="mt-1 text-sm text-error-500">{{ phoneError }}</p>
            </div>

            <!-- 구분선 -->
            <div class="col-span-2">
              <hr class="my-6 border-gray-200 dark:border-gray-700" />
            </div>

            <!-- 비밀번호 변경 -->
            <form @submit.prevent  class="contents">
              <!-- id-password의 안정적인 구조를 만들어 경고 제거 -->
              <input type="text" name="username" autocomplete="username" hidden />

              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">비밀번호 변경</h3>
                  <div class="flex gap-2">
                    <button
                        v-if="!pwEditing"
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        :disabled="changingPw"
                        @click="onPwEdit"
                    >수정</button>

                    <template v-else>
                      <button
                          type="button"
                          class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                          :disabled="changingPw"
                          @click="onPwCancel"
                      >취소</button>
                      <button
                          type="button"
                          class="h-9 rounded-lg bg-brand-500 px-3 text-white hover:bg-brand-600 disabled:opacity-50"
                          :disabled="changingPw"
                          @click="changePassword"
                      >{{ changingPw ? '변경 중...' : '변경' }}</button>
                    </template>
                  </div>
                </div>
              </div>

              <!-- 현재 비밀번호 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">현재 비밀번호</div>
              <div class="col-start-2">
                <div class="relative">
                  <input
                      v-model="currentPassword"
                      :type="showCurrentPw ? 'text' : 'password'"
                      placeholder="현재 비밀번호"
                      :disabled="!pwEditing || changingPw"
                      autocomplete="current-password"
                      class="h-11 w-full rounded-lg border px-3
                         bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                         dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                         disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                         dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                      @blur="validateCurrentPassword"
                  />
                  <span
                      @click="pwEditing && !changingPw ? toggleCurrentPwVisibility() : null"
                      :class="['absolute right-4 top-1/2 -translate-y-1/2',
                 (!pwEditing || changingPw) ? 'pointer-events-none opacity-50' : 'cursor-pointer']"
                  >
                    <EyeIcon v-if="showCurrentPw" class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                    <EyeSlashIcon v-else class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                  </span>
                </div>
                <p v-if="currentPwError" class="text-sm text-error-500">{{ currentPwError }}</p>
              </div>

              <!-- 새 비밀번호 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">새 비밀번호</div>
              <div class="col-start-2">
                <div class="relative">
                  <input
                      v-model="newPassword"
                      :type="showNewPw ? 'text' : 'password'"
                      placeholder="새 비밀번호 (6자 이상)"
                      :disabled="!pwEditing || changingPw"
                      autocomplete="new-password"
                      class="h-11 w-full rounded-lg border px-3
                         bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                         dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                         disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                         dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                      @blur="validateNewPassword"
                  />
                  <span
                      @click="pwEditing && !changingPw ? togglePasswordVisibility() : null"
                      :class="['absolute right-4 top-1/2 -translate-y-1/2',
                 (!pwEditing || changingPw) ? 'pointer-events-none opacity-50' : 'cursor-pointer']"
                  >
                    <EyeIcon v-if="showNewPw" class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                    <EyeSlashIcon v-else class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                  </span>
                </div>
                <p v-if="newPwError" class="text-sm text-error-500">{{ newPwError }}</p>
              </div>

              <!-- 새 비밀번호 확인 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">비밀번호 확인</div>
              <div class="col-start-2">
                <div class="relative">
                  <input
                      v-model="newPassword2"
                      :type="showNewPw2 ? 'text' : 'password'"
                      placeholder="새 비밀번호 확인"
                      :disabled="!pwEditing || changingPw"
                      autocomplete="new-password"
                      class="h-11 w-full rounded-lg border px-3
                         bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                         dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                         disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                         dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                      @blur="validateNewPasswordConfirm"
                  />
                  <span
                      @click="pwEditing && !changingPw ? togglePassword2Visibility() : null"
                      :class="['absolute right-4 top-1/2 -translate-y-1/2',
                 (!pwEditing || changingPw) ? 'pointer-events-none opacity-50' : 'cursor-pointer']"
                  >
                    <EyeIcon v-if="showNewPw2" class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                    <EyeSlashIcon v-else class="w-5 h-5 text-gray-500 dark:text-gray-400" />
                  </span>
                </div>
                <p v-if="newPw2Error" class="text-sm text-error-500">{{ newPw2Error }}</p>
              </div>
            </form>

            <!-- SUPER 이메일 전용 -->
            <template v-if="isSuperEmail">

              <!-- ===== 접속 로그 다운로드 ===== -->
              <div class="col-span-2">
                <hr class="my-6 border-gray-200 dark:border-gray-700" />
              </div>

              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                    접속 로그 다운로드 (기간별 Excel)
                  </h3>
                </div>
              </div>

              <!-- 날짜 선택 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">기간</div>
              <div class="col-start-2">
                <div class="flex gap-2">
                  <!-- 날짜 필터 -->
                  <div class="inline-flex items-stretch whitespace-nowrap">
                    <!-- 시작일 -->
                    <input
                        type="text"
                        ref="logStartInput"
                        class="w-full h-11 border border-gray-200 dark:border-gray-700 rounded-l-lg px-3 py-2 text-sm text-center
                             focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                             dark:bg-gray-800 dark:text-gray-200"
                        placeholder="시작일"
                    />

                    <!-- 구분자 -->
                    <span
                        class="flex items-center justify-center w-20 h-11 border-t border-b border-gray-200 dark:border-gray-700
                              text-sm text-gray-500 dark:text-gray-400 bg-gray-50 dark:bg-gray-900">~</span>

                    <!-- 종료일 -->
                    <input
                        type="text"
                        ref="logEndInput"
                        class="w-full h-11 border border-gray-200 dark:border-gray-700 rounded-r-lg px-3 py-2 text-sm text-center
                           focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                           dark:bg-gray-800 dark:text-gray-200"
                        placeholder="종료일"
                    />
                  </div>
                  <button
                      type="button"
                      class="ml-auto h-11 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                      :disabled="downloading || !logFrom || !logTo"
                      @click="downloadLogs"
                  >
                    엑셀 다운로드
                  </button>
                </div>
                <p v-if="logRangeError" class="mt-1 text-sm text-error-500">{{ logRangeError }}</p>
              </div>

              <!-- ===== google 스프레드 시트 연동 ===== -->
              <div class="col-span-2">
                <hr class="my-6 border-gray-200 dark:border-gray-700" />
              </div>

              <!-- 제목 + 우측 버튼 -->
              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Google 스프레드시트 연동</h3>
                  <div class="flex gap-2">
                    <button
                        v-if="!sheetEditing"
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        @click="sheetEditing = true"
                    >수정</button>
                    <template v-else>
                      <button
                          type="button"
                          class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                          :disabled="savingSheet"
                          @click="onSheetCancel"
                      >취소</button>
                      <button
                          type="button"
                          class="h-9 rounded-lg bg-brand-500 px-3 text-white hover:bg-brand-600 disabled:opacity-50"
                          :disabled="savingSheet"
                          @click="onSheetSave"
                      >{{ savingSheet ? '저장 중...' : '저장' }}</button>
                    </template>
                  </div>
                </div>
              </div>

              <!-- Spreadsheet ID -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">Spreadsheet ID</div>
              <div class="col-start-2">
                <input
                    v-model="sheetId"
                    :disabled="savingSheet || !sheetEditing"
                    placeholder="예) 1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
                    @blur="validateSheetId"
                    class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                     focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                     disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                     dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                />
                <p v-if="sheetIdError" class="mt-1 text-sm text-error-500">{{ sheetIdError }}</p>
              </div>

              <!-- 시트 제목 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">시트 제목</div>
              <div class="col-start-2">
                <input
                    v-model="sheetName"
                    :disabled="savingSheet || !sheetEditing"
                    placeholder="예) Sheet1"
                    @blur="validateSheetName"
                    class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                     focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                     disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                     dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                />
                <p v-if="sheetNameError" class="mt-1 text-sm text-error-500">{{ sheetNameError }}</p>
              </div>

              <!-- 시작 행 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300">마지막 행</div>
              <div class="col-start-2">
                <input
                    v-model.number="startRow"
                    :disabled="savingSheet || !sheetEditing"
                    type="number"
                    min="1"
                    step="1"
                    placeholder="예) 2"
                    @blur="validateStartRow"
                    class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                     focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                     disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                     dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                />
                <p v-if="startRowError" class="mt-1 text-sm text-error-500">{{ startRowError }}</p>
              </div>

              <!-- ===== IP 화이트리스트 ===== -->
              <div class="col-span-2">
                <hr class="my-6 border-gray-200 dark:border-gray-700" />
              </div>

              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                    IP 화이트리스트
                  </h3>
                  <div class="flex gap-2">
                    <button
                        v-if="!ipEditing"
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100
                             dark:bg-gray-700 dark:text-gray-100"
                        @click="ipEditing = true"
                    >
                      수정
                    </button>
                    <button
                        v-else
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100
                             dark:bg-gray-700 dark:text-gray-100"
                        @click="onIpCancel"
                    >
                      취소
                    </button>
                  </div>
                </div>
              </div>

              <!-- 수정 모드에서만 새 IP 등록 폼 노출 (DOM 유지: v-show) -->
              <div class="contents" v-show="ipEditing">
                <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">
                  새 IP 등록
                </div>
                <div class="col-start-2">
                  <div class="flex flex-col gap-2">
                    <div class="flex flex-col gap-2 xl:flex-row">
                      <input
                          v-model="newIp"
                          :disabled="ipSaving"
                          placeholder="예) 118.235.10.27"
                          class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                               focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                               dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                      />
                      <input
                          v-model="newIpMemo"
                          :disabled="ipSaving"
                          placeholder="메모 (예: 본사 팀장 PC)"
                          class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                               focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                               dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                      />
                      <button
                          type="button"
                          class="h-11 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white
                               hover:bg-brand-600 disabled:opacity-50"
                          :disabled="ipSaving || !canAddIp"
                          @click="addIp"
                      >
                        {{ ipSaving ? '추가 중...' : '추가' }}
                      </button>
                    </div>
                    <p v-if="ipFormError" class="text-xs text-error-500">
                      {{ ipFormError }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- IP 검색 (편집 여부와 상관없이 항상 표시) -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-4">
                검색
              </div>
              <div class="col-start-2">
                <div class="flex gap-2 w-full">
                  <!-- 입력창 + X 버튼 -->
                  <div class="relative flex-1">
                    <input
                        v-model="ipSearchInput"
                        type="text"
                        placeholder="IP, 메모, 수정일(YYYY-MM-DD) 검색"
                        @keyup.enter="applyIpSearch"
                        class="h-10 w-full rounded-lg border px-3 pr-8 bg-white text-gray-800
                            focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                            dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                            disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                            dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                    />
                    <!-- 값 있을 때는 항상 X 보이게 -->
                    <button
                        v-if="ipSearchInput"
                        type="button"
                        class="absolute inset-y-0 right-4 my-auto text-xs text-gray-400 hover:text-gray-600
                              dark:text-gray-500 dark:hover:text-gray-300"
                        @click="clearIpSearch"
                    >
                      ✕
                    </button>
                  </div>

                  <!-- 검색 버튼 (항상 파란색, 비활성 없음) -->
                  <button
                      type="button"
                      class="h-10 px-4 rounded-lg bg-brand-500 text-xs sm:text-sm font-medium text-white
                            hover:bg-brand-600 disabled:opacity-50 disabled:cursor-not-allowed"
                      @click="applyIpSearch"
                  >
                    검색
                  </button>
                </div>
              </div>

              <!-- 등록된 IP 목록 : 항상 표시 & 그리드 2열 전체 사용 -->
              <div class="col-span-2 mt-4">
                <div
                    class="ip-scroll rounded-md border border-gray-100 dark:border-gray-800
                         max-h-85 overflow-auto spin-dark"
                >
                  <!-- 로딩 -->
                  <div
                      v-if="ipLoading"
                      class="p-4 text-sm text-gray-500 dark:text-gray-400"
                  >
                    불러오는 중…
                  </div>

                  <!-- 비어있음 -->
                  <div
                      v-else-if="!ipList || ipList.length === 0"
                      class="p-4 text-sm text-gray-500 dark:text-gray-400"
                  >
                    등록된 IP가 없습니다.
                  </div>

                  <!-- 검색 결과 없음 -->
                  <div
                      v-else-if="filteredIpList.length === 0"
                      class="p-4 text-sm text-gray-500 dark:text-gray-400"
                  >
                    검색 결과가 없습니다.
                  </div>

                  <!-- 테이블 -->
                  <table
                      v-else
                      class="min-w-full table-fixed text-sm"
                  >
                    <thead
                        class="sticky top-0 z-10 bg-white dark:bg-gray-900
                             shadow-[0_1px_0_0_rgba(229,231,235,0.7)]
                             dark:shadow-[0_1px_0_0_rgba(31,41,55,0.8)]"
                    >
                    <tr
                        class="border-b border-gray-200 dark:border-gray-700
                               text-xs text-gray-500 dark:text-gray-400"
                    >
                      <th class="w-10 px-3 py-2 text-center whitespace-nowrap">번호</th>
                      <th class="px-3 py-2 text-left">IP / 메모</th>
                      <th class="w-20 px-3 py-2 text-center whitespace-nowrap">상태</th>
                      <th class="w-28 px-3 py-2 text-right whitespace-nowrap">수정일</th>
                      <th class="w-20 px-3 py-2 text-center whitespace-nowrap">관리</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr
                        v-for="(row, idx) in filteredIpList"
                        :key="row.ipId ?? idx"
                        :class="[
                          'border-b border-gray-100 dark:border-gray-800',
                          idx % 2 === 0
                            ? 'bg-white dark:bg-gray-900'
                            : 'bg-gray-50 dark:bg-gray-800/60'
                        ]"
                    >
                      <!-- 행번호 -->
                      <td class="px-3 py-3 text-center text-xs text-gray-400 whitespace-nowrap">
                        {{ idx + 1 }}
                      </td>

                      <!-- IP / 메모 두 줄 (메모만 줄바꿈 허용) -->
                      <td class="px-3 py-3 align-middle">
                        <!-- 편집 모드 -->
                        <template v-if="editingRowId === row.ipId">
                          <input
                              v-model="editingRow.ipAddress"
                              class="mb-1 w-full rounded-md border border-gray-200 px-2 py-1 text-xs
                                     bg-white text-gray-800
                                     focus:border-brand-300 focus:outline-hidden focus:ring-2 focus:ring-brand-500/10
                                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                              placeholder="IP 주소"
                          />
                          <input
                              v-model="editingRow.memo"
                              class="w-full rounded-md border border-gray-200 px-2 py-1 text-xs
                                     bg-white text-gray-800
                                     focus:border-brand-300 focus:outline-hidden focus:ring-2 focus:ring-brand-500/10
                                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                              placeholder="메모"
                          />
                        </template>

                        <!-- 표시 모드 -->
                        <template v-else>
                          <div
                              class="font-mono text-[13px] text-gray-900 dark:text-gray-100 truncate"
                          >
                            {{ row.ipAddress }}
                          </div>
                          <div
                              class="mt-0.5 text-xs text-gray-400 dark:text-gray-500 break-words"
                          >
                            {{ row.memo || '-' }}
                          </div>
                        </template>
                      </td>

                      <!-- 상태 -->
                      <td class="px-3 py-3 text-center whitespace-nowrap">
                        <!-- 편집 모드 -->
                        <template v-if="editingRowId === row.ipId">
                          <select
                              v-model="editingRow.isActive"
                              class="h-8 rounded-md border border-gray-300 px-2 text-xs
                                     bg-white text-gray-800
                                     focus:border-brand-300 focus:outline-hidden focus:ring-2 focus:ring-brand-500/10
                                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                          >
                            <option value="Y">사용</option>
                            <option value="N">중지</option>
                          </select>
                        </template>

                        <!-- 표시 모드 -->
                        <template v-else>
                            <span
                                class="inline-flex items-center justify-center rounded-full px-2 py-0.5 text-[11px] font-medium"
                                :class="row.isActive === 'Y'
                                ? 'bg-[#D1F2D6] text-[#2F855A] dark:bg-[#1B3A2E] dark:text-[#5FA97A]'
                                : 'bg-[#E5E7EB] text-[#4B5563] dark:bg-[#374151] dark:text-[#D1D5DB]'"
                            >
                              {{ row.isActive === 'Y' ? '사용' : '중지' }}
                            </span>
                        </template>
                      </td>

                      <!-- 수정일: 날짜 / 시간 두 줄 -->
                      <td
                          class="px-3 py-3 text-right text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap"
                      >
                        <template v-if="row.updatedAt || row.createdAt">
                            <span class="block">
                              {{ splitDateTime(row.updatedAt || row.createdAt)[0] }}
                            </span>
                          <span class="block">
                              {{ splitDateTime(row.updatedAt || row.createdAt)[1] }}
                            </span>
                        </template>
                        <span v-else>-</span>
                      </td>

                      <!-- 관리: 수정 / 저장·취소 / 삭제 -->
                      <td class="px-3 py-3 text-right whitespace-nowrap">
                        <template v-if="editingRowId === row.ipId">
                          <button
                              type="button"
                              class="mr-1 px-2 py-1 text-xs rounded-md border border-brand-500
                                     bg-brand-500 text-white hover:bg-brand-600
                                     disabled:opacity-50"
                              :disabled="ipSaving"
                              @click="saveIpRow(row)"
                          >
                            저장
                          </button>
                          <button
                              type="button"
                              class="px-2 py-1 text-xs rounded-md border border-gray-200 dark:border-gray-700
                                     bg-white text-gray-700 hover:bg-gray-50
                                     dark:bg-transparent dark:text-gray-200 dark:hover:bg-gray-700
                                     disabled:opacity-50"
                              :disabled="ipSaving"
                              @click="cancelIpRowEdit"
                          >
                            취소
                          </button>
                        </template>

                        <template v-else>
                          <button
                              type="button"
                              class="px-2 py-1 text-xs rounded-md border border-gray-200 dark:border-gray-700
                                     bg-white text-gray-700 hover:bg-gray-50
                                     dark:bg-transparent dark:text-gray-200 dark:hover:bg-gray-700
                                     disabled:opacity-50"
                              :disabled="!ipEditing || ipSaving"
                              @click="startIpRowEdit(row)"
                          >
                            수정
                          </button>
                          <button
                              type="button"
                              class="ml-1 px-2 py-1 text-xs rounded-md border border-red-200 dark:border-red-700
                                     bg-white text-red-600 hover:bg-red-50
                                     dark:bg-transparent dark:text-red-400 dark:hover:bg-red-900/40
                                     disabled:opacity-50"
                              :disabled="!ipEditing || ipSaving"
                              @click="deleteIpRow(row)"
                          >
                            삭제
                          </button>
                        </template>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- ===== 센터 관리 ===== -->
              <div class="col-span-2">
                <hr class="my-6 border-gray-200 dark:border-gray-700" />
              </div>

              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">센터 관리</h3>
                  <div class="flex gap-2">
                    <button
                        v-if="!centersEditing"
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        @click="centersEditing = true"
                    >수정</button>
                    <button
                        v-else
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        @click="centersEditing = false"
                    >취소</button>
                  </div>
                </div>
              </div>

              <!-- 신규 센터 추가 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">신규 센터</div>
              <div class="col-start-2">
                <div class="flex gap-2">
                  <input
                      v-model="newCenterName"
                      :disabled="!centersEditing"
                      placeholder="예) 강남센터"
                      class="h-11 w-full rounded-lg border px-3 bg-white text-gray-800
                     focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                     disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                     dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                  />
                  <button
                      type="button"
                      class="h-11 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                      :disabled="!centersEditing"
                      @click="addCenter"
                  >추가</button>
                </div>
              </div>

              <!-- 목록 -->
              <div v-if="centersEditing"
                   class="text-sm font-medium text-gray-700 dark:text-gray-300">센터 목록</div>

              <!-- 리스트 컨테이너: 스크롤, 라운드, 테두리 -->
              <div v-if="centersEditing"
                   class="rounded-md border border-gray-100 dark:border-gray-800 max-h-50 overflow-auto">

                <!-- 로딩 -->
                <div v-if="centersLoading" class="p-4 text-sm text-gray-500 dark:text-gray-400">
                  불러오는 중...
                </div>

                <!-- 비어있음 -->
                <div v-else-if="!centers || centers.length === 0" class="p-4 text-sm text-gray-500 dark:text-gray-400">
                  조회 결과가 없습니다.
                </div>

                <!-- 리스트 -->
                <ul v-else>
                  <li
                      v-for="c in centers"
                      :key="c.centerId"
                      class="flex items-center justify-between px-3 py-2 hover:bg-gray-50 dark:hover:bg-gray-800/70"
                  >
                    <div class="text-sm text-gray-800 dark:text-gray-200">
                      {{ c.centerName }}
                    </div>

                    <!-- 흰색 아웃라인 버튼 -->
                    <button
                        type="button"
                        class="px-2 py-1 text-xs rounded-md border border-gray-200 dark:border-gray-700
                             bg-white text-gray-700 hover:bg-gray-50
                             dark:bg-transparent dark:text-gray-200 dark:hover:bg-gray-700
                             focus:outline-hidden focus:ring-2 focus:ring-gray-200 dark:focus:ring-gray-700
                             disabled:opacity-50"
                        :disabled="!centersEditing"
                        @click.stop="deleteCenter(c.centerId)"
                    >
                      삭제
                    </button>
                  </li>
                </ul>
              </div>

              <!-- ===== 슈퍼계정 위임 ===== -->
              <div class="col-span-2">
                <hr class="my-6 border-gray-200 dark:border-gray-700" />
              </div>

              <div class="col-span-2">
                <div class="flex items-center justify-between">
                  <h3 class="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">슈퍼계정 위임</h3>
                  <div class="flex gap-2">
                    <button
                        v-if="!delegateEditing"
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        @click="onDelegateEdit"
                    >수정</button>
                    <button
                        v-else
                        type="button"
                        class="h-9 rounded-lg bg-gray-200 px-3 text-gray-800 hover:bg-gray-100 dark:bg-gray-700 dark:text-gray-100"
                        @click="onDelegateCancel"
                    >취소</button>
                  </div>
                </div>
              </div>

              <!-- 대상 ID 입력 -->
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mt-2">대상 사용자 ID(Email)</div>
              <div class="col-start-2">
                <div class="flex gap-2">
                  <input
                      v-model="delegateIdInput"
                      placeholder="예) example@naver.com"
                      :disabled="!delegateEditing"
                      class="h-11 w-full rounded-lg border px-3
                           bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                           dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100
                           disabled:bg-gray-50 disabled:text-gray-400 disabled:border-gray-200
                           dark:disabled:bg-gray-900/40 dark:disabled:text-gray-500 dark:disabled:border-gray-700"
                  />
                  <button
                      type="button"
                      class="h-11 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                      :disabled="!delegateEditing || lookingUp || !delegateIdInput"
                      @click="lookupDelegate"
                  >
                    {{ lookingUp ? '조회 중...' : '검색' }}
                  </button>
                </div>
              </div>
              <div class="col-start-2" v-if="delegateLookupError">
                <p class="mt-1 text-sm text-error-500">{{ delegateLookupError }}</p>
              </div>

              <!-- 조회 결과 카드 -->
              <template v-if="delegateEditing && candidate">
                <div class="col-span-2"></div>
                <div ref="delegateCardRef" class="col-start-2 w-full rounded-lg border border-gray-200 dark:border-gray-800 p-4">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="text-sm text-gray-600 dark:text-gray-400">
                        이름 :
                        <b class="text-gray-800 dark:text-gray-200">{{ candidate.userName }}</b>
                      </p>
                      <p class="text-sm text-gray-600 dark:text-gray-400">
                        이메일 :
                        <b class="text-gray-800 dark:text-gray-200">{{ candidate.userEmail }}</b>
                      </p>
                      <p class="text-sm text-gray-600 dark:text-gray-400">
                        구분/소속 :
                        <b class="text-gray-800 dark:text-gray-200">
                          {{ roleHuman(candidate.userRole) }} / {{ candidate.centerName || '미할당' }}
                        </b>
                      </p>

                      <p class="mt-2 text-xs text-gray-500 dark:text-gray-400">
                        * 위임 후 현재 계정의 슈퍼 권한은 해제됩니다. 진행 전에 꼭 확인하세요.
                      </p>
                    </div>
                    <button
                        type="button"
                        class="h-10 shrink-0 rounded-lg bg-brand-500 px-4 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                        :disabled="!delegateEditing || delegating"
                        @click="delegateNow"
                    >
                      {{ delegating ? '위임 중...' : '위임 확정' }}
                    </button>
                  </div>
                </div>
              </template>

            </template>

<!--          </template>-->

          <!-- 바닥 여백 확보를 위한 구분선 -->
          <div class="col-span-2 mb-6">
<!--            <hr class="my-6 border-gray-200 dark:border-gray-700" />-->
          </div>

        </div>
      </div>
    </div>
  </AdminLayout>

  <!-- 전역 로딩 오버레이 (메모 모달과 동일하게 body로 텔레포트) -->
  <Teleport to="body">
    <transition name="fade">
      <div
          v-if="downloading"
          class="fixed inset-0 z-[2147483646]"
          aria-live="polite" aria-busy="true" role="status"
      >
        <!-- 배경 -->
        <div class="absolute inset-0 bg-black/5 dark:bg-black/60"></div>

        <!-- 스피너 -->
        <div class="absolute inset-0 z-[2147483647] flex items-center justify-center">
          <div class="flex flex-col items-center gap-3">
            <svg class="animate-spin h-8 w-8 text-blue-500" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10"
                      stroke="currentColor" stroke-width="4" fill="none"/>
              <path class="opacity-75" fill="currentColor"
                    d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"/>
            </svg>
            <p class="text-sm text-gray-900 dark:text-white/90">불러오는 중…</p>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import CommonGridShape from '@/components/common/CommonGridShape.vue'
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/outline'
import axios from '@/plugins/axios.js'
import { onBeforeRouteLeave } from 'vue-router'

/* ===== 기본 프로필 ===== */
const email = ref('')
const name  = ref('')
const phone = ref('')

const roleLabel = ref('-')
const orgLabel  = ref('-')

/* 전화번호 */
const phoneInput  = ref('')
const savingPhone = ref(false)
const phoneError  = ref('')

/* 비밀번호 */
const currentPassword = ref('')
const newPassword     = ref('')
const newPassword2    = ref('')
const changingPw      = ref(false)

const currentPwError = ref('')
const newPwError     = ref('')
const newPw2Error    = ref('')

/* SUPER(스프레드시트) */
const isSuperEmail = ref(false)
const sheetId       = ref('')
const startRow      = ref(1)
const savingSheet   = ref(false)
const sheetIdError  = ref('')
const startRowError = ref('')
const sheetName      = ref('')
const sheetNameError = ref('')

const sheetEditing  = ref(false)
const originalSheet = ref({ sheetId: '', startRow: 1, sheetName: '' })

/* 비번 보기 토글 */
const showCurrentPw = ref(false)
const showNewPw     = ref(false)
const showNewPw2    = ref(false)
const toggleCurrentPwVisibility = () => { showCurrentPw.value = !showCurrentPw.value }
const togglePasswordVisibility   = () => { showNewPw.value = !showNewPw.value }
const togglePassword2Visibility  = () => { showNewPw2.value = !showNewPw2.value }

// 비밀번호 변경 편집 토글
const pwEditing = ref(false)
function onPwEdit() { pwEditing.value = true }
function onPwCancel() {
  pwEditing.value = false
  resetPwForm()
}

onMounted(async () => {
  await loadMe()

  // 스프레드시트 설정 초기 로드
  await loadSheetConfig()
})

/* 서버에서 내 정보 로드 */
async function loadMe() {
  try {
    const { data } = await axios.get('/api/me', { withCredentials: true })
    name.value  = data.userName
    email.value = data.userEmail
    phone.value = data.userPhone

    console.log(data)
    if (data.grants.role === 'SUPERADMIN') roleLabel.value = '관리자'
    else if (data.grants.role === 'MANAGER') roleLabel.value = '팀장'
    else if (data.grants.role === 'STAFF') roleLabel.value = '프로'
    else if (data.grants.role === 'CENTERHEAD') roleLabel.value = '센터장'
    else if (data.grants.role === 'EXPERT') roleLabel.value = '전문가'
    else roleLabel.value = '-'

    orgLabel.value   = data.centerName || '미할당'
    isSuperEmail.value = !!data.grants.isSuper

    phoneInput.value = phone.value || ''
  } catch (_) {
    /* 프로필 로드 실패는 조용히 무시 */
  }
}

/* ===== 전화번호 ===== */
const formatPhoneInput = () => {
  let digits = (phoneInput.value || '').replace(/\D/g, '')
  if (digits.length <= 3) phoneInput.value = digits
  else if (digits.length <= 7) phoneInput.value = `${digits.slice(0,3)}-${digits.slice(3)}`
  else phoneInput.value = `${digits.slice(0,3)}-${digits.slice(3,7)}-${digits.slice(7,11)}`
}
const validatePhoneInput = () => {
  if (!phoneInput.value) { phoneError.value = ''; return }
  phoneError.value = /^010-\d{4}-\d{4}$/.test(phoneInput.value)
      ? '' : '전화번호는 010-1234-5678 형식으로 입력해야 합니다.'
}
async function savePhone() {
  validatePhoneInput()
  if (phoneError.value) {
    alert(phoneError.value)
    return
  }
  try {
    savingPhone.value = true
    await axios.put('/api/me/phone', phoneInput.value, {
      headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
      withCredentials: true
    })
    phone.value = phoneInput.value
    alert('전화번호가 저장되었습니다.')
  } catch (e) {
    alert(e?.response?.data || '전화번호 저장 실패')
  } finally {
    savingPhone.value = false
  }
}

/* ===== 비밀번호 ===== */
function resetPwForm() {
  currentPassword.value = ''
  newPassword.value = ''
  newPassword2.value = ''
  currentPwError.value = ''
  newPwError.value = ''
  newPw2Error.value = ''
}
const validateCurrentPassword = () => {
  currentPwError.value = currentPassword.value ? '' : ''
}
const validateNewPassword = () => {
  if (!newPassword.value) { newPwError.value = ''; return }
  newPwError.value = newPassword.value.length < 6 ? '비밀번호는 최소 6자리 이상이어야 합니다.' : ''
}
const validateNewPasswordConfirm = () => {
  if (!newPassword2.value) { newPw2Error.value = ''; return }
  newPw2Error.value = (newPassword.value !== newPassword2.value) ? '비밀번호가 일치하지 않습니다.' : ''
}
watch([newPassword, newPassword2], () => {
  if (!newPassword2.value) { newPw2Error.value = ''; return }
  newPw2Error.value = (newPassword.value !== newPassword2.value) ? '비밀번호가 일치하지 않습니다.' : ''
})
async function changePassword() {
  if (!currentPassword.value || !newPassword.value) {
    currentPwError.value = !currentPassword.value ? '현재 비밀번호를 입력하세요.' : ''
    newPwError.value = !newPassword.value ? '새 비밀번호를 입력하세요.' : newPwError.value
    alert('비밀번호를 입력하세요.')
    return
  }
  if (newPassword.value.length < 6) {
    newPwError.value = '비밀번호는 최소 6자리 이상이어야 합니다.'
    alert('새 비밀번호는 6자 이상이어야 합니다.')
    return
  }
  if (newPassword.value !== newPassword2.value) {
    newPw2Error.value = '비밀번호가 일치하지 않습니다.'
    alert('새 비밀번호가 일치하지 않습니다.')
    return
  }

  try {
    changingPw.value = true
    await axios.put('/api/me/password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    }, { withCredentials: true })
    alert('비밀번호가 변경되었습니다. 다음 로그인부터 적용됩니다.')
    resetPwForm()
  } catch (e) {
    if (e.response?.status === 400) {
      alert(e?.response?.data || '입력값 확인')
    } else if (e.response?.status === 401) {
      alert('다시 로그인 후 시도하세요.')
    } else {
      alert('변경 실패')
    }
  } finally {
    changingPw.value = false
  }
}

/* ===== 스프레드시트 ===== */
async function loadSheetConfig() {
  try {
    const { data } = await axios.get('/api/me/sheet-settings', { withCredentials: true })

    // 데이터 로드
    sheetId.value  = (data?.sheetId ?? '')
    startRow.value = Number(data?.startRow ?? 1)
    sheetName.value = (data?.sheetName ?? '')

    originalSheet.value = { sheetId: sheetId.value, startRow: startRow.value, sheetName: sheetName.value }
    sheetEditing.value = false // 초기엔 읽기전용
  } catch {
    // 값 없으면 기본값 유지, 편집 off
    sheetEditing.value = false
  }
}

async function onSheetSave() {
  await saveSheetConfig()
  originalSheet.value = { sheetId: sheetId.value, startRow: startRow.value, sheetName: sheetName.value }
  sheetEditing.value = false
}
function onSheetCancel() {
  sheetId.value  = originalSheet.value.sheetId
  startRow.value = originalSheet.value.startRow
  sheetName.value = originalSheet.value.sheetName
  sheetEditing.value = false
}

function validateSheetId() {
  const v = (sheetId.value || '').trim()
  sheetIdError.value = v ? '' : 'Spreadsheet ID를 입력하세요.'
}
function validateSheetName() {
  const v = (sheetName.value || '').trim()
  sheetNameError.value = v ? '' : '시트 제목을 입력하세요.'
}
function validateStartRow() {
  const n = Number(startRow.value || 0)
  startRowError.value = n >= 1 ? '' : '시작 행은 1 이상의 정수여야 합니다.'
}
function resetSheetForm() {
  sheetId.value = ''
  startRow.value = 1
  sheetIdError.value = ''
  startRowError.value = ''
}
async function saveSheetConfig() {
  validateSheetId()
  validateSheetName()
  validateStartRow()
  if (sheetIdError.value || startRowError.value || sheetNameError.value) {
    alert('입력값을 확인하세요.')
    return
  }

  if(!confirm("스프레드시트 연동 정보를 정말 변경하시겠습니까?")) {
    onSheetCancel()
    return
  }

  try {
    savingSheet.value = true
    await axios.put('/api/me/sheet-settings', {
      sheetId: sheetId.value,
      startRow: startRow.value,
      sheetName: sheetName.value
    }, { withCredentials: true })
    alert('시트 설정이 저장되었습니다.')
  } catch (e) {
    alert(e?.response?.data || '시트 정보 저장 실패')
  } finally {
    savingSheet.value = false
  }
}

/* ===== 센터관리 ===== */
const centers = ref([]);
const centersLoading = ref(false)
const centersEditing = ref(false)
const newCenterName = ref('')

async function fetchCenters() {
  try {
    centersLoading.value = true;
    const { data } = await axios.get('/api/me/centers', { withCredentials: true });
    centers.value = Array.isArray(data) ? data : [];
  } catch (e) {
    alert(e?.response?.data || '센터 목록을 불러오지 못했습니다.');
  } finally {
    centersLoading.value = false;
  }
}

async function addCenter() {
  const name = (newCenterName.value || '').trim();
  if (!name) return alert('센터 이름을 입력하세요.');

  try {
    await axios.post('/api/me/centers', { centerName: name }, { withCredentials: true });
    newCenterName.value = '';
    await fetchCenters();
  } catch (e) {
    const s = e?.response?.status;
    if (s === 409) {
      alert(e?.response?.data || '이미 존재하는 센터명입니다.');
    } else if (s === 400) {
      alert(e?.response?.data || '요청 값이 올바르지 않습니다.');
    } else if (s === 403) {
      alert('접근 권한이 없습니다.');
    } else {
      alert('센터 추가에 실패했습니다.');
    }
  }
}

async function deleteCenter(id) {
  const cid = Number(id);
  if (!Number.isFinite(cid)) return alert('잘못된 ID');
  if (!confirm('삭제하시겠습니까?')) return;

  try {
    await axios.delete(`/api/me/centers/${cid}`, { withCredentials: true });
    await fetchCenters();
  } catch (e) {
    const s = e?.response?.status;
    if (s === 409) {
      alert(e?.response?.data || '센터에 소속된 직원이 있어 삭제할 수 없습니다.');
    } else if (s === 404) {
      alert(e?.response?.data || '존재하지 않는 센터입니다.');
    } else if (s === 403) {
      alert('접근 권한이 없습니다.');
    } else {
      alert('센터 삭제에 실패했습니다.');
    }
  }
}

// -----------------------

// 날짜 피커 + 다운로드 + 위임용 상태/메서드 (상단 import 아래쪽에 추가)
import flatpickr from 'flatpickr'
import { Korean } from 'flatpickr/dist/l10n/ko.js'
import 'flatpickr/dist/flatpickr.css'

// ====== 접속 로그 다운로드 ======
const logStartInput = ref(null)
const logEndInput   = ref(null)
const logFrom = ref(null) // Date | null
const logTo   = ref(null) // Date | null
const logRangeError = ref('')
const downloading = ref(false)

let fpStart = null
let fpEnd = null

function mountLogPickers() {
  // 시작일
  try { fpStart?.destroy() } catch {}
  if (logStartInput.value) {
    fpStart = flatpickr(logStartInput.value, {
      locale: Korean,
      dateFormat: 'Y-m-d',
      allowInput: true,
      disableMobile: true,
      onReady: (_d, _s, fp) => {
        fp.calendarContainer.style.zIndex = '999999'
        // --- ✕ 클리어 버튼 (중복 방지) ---
        const anyFp = fp
        if (!anyFp._clearBtn) {
          const btn = document.createElement('button')
          btn.type = 'button'
          btn.title = '입력값 지우기'
          btn.textContent = '✕'
          btn.className =
              'fp-clear-btn absolute top-1 right-1 w-6 h-6 flex items-center justify-center ' +
              'text-xs text-gray-500 hover:text-gray-700 rounded'
          btn.addEventListener('mousedown', (e) => {
            e.preventDefault()
            e.stopPropagation()
            fp.clear()   // 로그 시작일 제거
            logFrom.value = null
            fp.close()
          })
          fp.calendarContainer.appendChild(btn)
          anyFp._clearBtn = btn
        }
      },
      onChange: (dates) => { logFrom.value = dates[0] ?? null }
    })
    if (logFrom.value) fpStart.setDate(logFrom.value, false, 'Y-m-d')
  }

  // 종료일
  try { fpEnd?.destroy() } catch {}
  if (logEndInput.value) {
    fpEnd = flatpickr(logEndInput.value, {
      locale: Korean,
      dateFormat: 'Y-m-d',
      allowInput: true,
      disableMobile: true,
      onReady: (_d, _s, fp) => {
        fp.calendarContainer.style.zIndex = '999999'
        // --- ✕ 클리어 버튼 (중복 방지) ---
        const anyFp = fp
        if (!anyFp._clearBtn) {
          const btn = document.createElement('button')
          btn.type = 'button'
          btn.title = '입력값 지우기'
          btn.textContent = '✕'
          btn.className =
              'fp-clear-btn absolute top-1 right-1 w-6 h-6 flex items-center justify-center ' +
              'text-xs text-gray-500 hover:text-gray-700 rounded'
          btn.addEventListener('mousedown', (e) => {
            e.preventDefault()
            e.stopPropagation()
            fp.clear()   // 로그 종료일 제거
            logTo.value = null
            fp.close()
          })
          fp.calendarContainer.appendChild(btn)
          anyFp._clearBtn = btn
        }
      },
      onChange: (dates) => { logTo.value = dates[0] ?? null }
    })
    if (logTo.value) fpEnd.setDate(logTo.value, false, 'Y-m-d')
  }
}
function cleanupLogPickers() {
  try { fpStart?.close(); fpStart?.destroy() } catch {}
  try { fpEnd?.close();   fpEnd?.destroy() } catch {}
  fpStart = fpEnd = null
}

function rebindLogPickers() {
  // DOM이 바뀐 뒤 실행되도록 한 템포 미룸
  nextTick().then(() => {
    cleanupLogPickers()
    mountLogPickers()
  })
}

// 윈도우 리사이즈 시 재바인딩
onMounted(() => window.addEventListener('resize', rebindLogPickers))
onUnmounted(() => window.removeEventListener('resize', rebindLogPickers))

// 스크롤/휠 시 열려있으면 닫기 (달력 잔상 방지)
const closeLogPickersOnScroll = () => {
  if (fpStart?.isOpen) fpStart.close()
  if (fpEnd?.isOpen) fpEnd.close()
}
onMounted(() => {
  window.addEventListener('wheel', closeLogPickersOnScroll, { passive: true, capture: true })
  window.addEventListener('scroll', closeLogPickersOnScroll, { passive: true, capture: true })
  window.addEventListener('touchmove', closeLogPickersOnScroll, { passive: true, capture: true })
})
onUnmounted(() => {
  window.removeEventListener('wheel', closeLogPickersOnScroll, true)
  window.removeEventListener('scroll', closeLogPickersOnScroll, true)
  window.removeEventListener('touchmove', closeLogPickersOnScroll, true)
})

onMounted(() => mountLogPickers())
onUnmounted(() => cleanupLogPickers())
watch([logStartInput, logEndInput], () => nextTick().then(mountLogPickers))

const ymd = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth()+1).padStart(2,'0')
  const day = String(d.getDate()).padStart(2,'0')
  return `${y}-${m}-${day}`
}

async function downloadLogs() {
  logRangeError.value = ''
  if (!logFrom.value || !logTo.value) return
  if (logFrom.value > logTo.value) {
    logRangeError.value = '시작일이 종료일보다 늦을 수 없습니다.'
    return
  }

  try {
    downloading.value = true
    // 백엔드: GET /api/super/logs/export?from=YYYY-MM-DD&to=YYYY-MM-DD
    const params = { from: ymd(logFrom.value), to: ymd(logTo.value) }
    const res = await axios.get('/api/me/logs/export', {
      params,
      responseType: 'blob',
      withCredentials: true
    })

    // 파일명 추출
    const cd = res.headers['content-disposition'] || ''
    const match = /filename\*\=UTF-8''([^;]+)/i.exec(cd)
    const fname = match ? decodeURIComponent(match[1]) : `login_logs_${params.from}_to_${params.to}.xlsx`

    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = fname
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    alert(e?.response?.data || '다운로드 실패')
  } finally {
    downloading.value = false
  }
}

// ====== 슈퍼계정 위임 ======
const delegateIdInput = ref(null)
const lookingUp = ref(false)
const delegating = ref(false)
const delegateLookupError = ref('')

// 스크롤 smooth
const delegateCardRef = ref(null)
function smoothScrollTo(el, offset = 80) {
  const rect = el.getBoundingClientRect()
  const scrollTopNow =
      (typeof window.scrollY === 'number' ? window.scrollY : null) ??
      document.scrollingElement?.scrollTop ??
      document.documentElement.scrollTop ??
      document.body.scrollTop ?? 0

  const top = Math.max(0, scrollTopNow + rect.top - offset)
  window.scrollTo({ top, behavior: 'smooth' })
}

// 슈퍼계정 위임 편집 토글
const delegateEditing = ref(false)
function onDelegateEdit() { delegateEditing.value = true }
function onDelegateCancel() {
  delegateEditing.value = false
  // 입력/에러/결과 초기화
  delegateIdInput.value = null
  delegateLookupError.value = ''
  candidate.value = null
}

const candidate = ref(null) // { userId, userName, userEmail, userRole, centerName? }

const roleHuman = (role) =>
    role === 'SUPERADMIN' ? '관리자'
        : role === 'MANAGER'  ? '팀장'
        : role === 'STAFF'    ? '프로'
        : role

async function lookupDelegate() {
  candidate.value = null
  delegateLookupError.value = ''

  // 이메일 형식 검사
  const email = String(delegateIdInput.value ?? '').trim()
  const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  if (!ok) {
    delegateLookupError.value = '올바른 이메일을 입력하세요.'
    return
  }

  try {
    lookingUp.value = true
    const { data } = await axios.get('/api/me/delegate/lookup', {
      params: { email },
      withCredentials: true
    })
    candidate.value = data
  } catch (e) {
    candidate.value = null
    const s = e?.response?.status
    if (s === 404) delegateLookupError.value = '대상 사용자를 찾을 수 없습니다.'
    else if (s === 403) delegateLookupError.value = '접근 권한이 없습니다.'
    else delegateLookupError.value = '조회 실패'
  } finally {
    lookingUp.value = false
  }

  await nextTick()
  if (delegateCardRef.value) smoothScrollTo(delegateCardRef.value, 80)
}

async function delegateNow() {
  if (!candidate.value) return
  const c = candidate.value
  const ok = confirm(
      `[위임 확인]\n\n대상: ${c.userName} (${c.userEmail})\n구분: ${roleHuman(c.userRole)} / ${c.centerName || '미할당'}\n\n정말 슈퍼 권한을 위 사용자에게 위임하시겠습니까?\n(현재 계정의 슈퍼 권한은 해제됩니다)`
  )
  if (!ok) return

  try {
    delegating.value = true
    await axios.post('/api/me/delegate', { userId: c.userId }, { withCredentials: true })
    alert('슈퍼 권한이 위임되었습니다. 다시 로그인해 주세요.')

    // 권한 위임 즉시 로그아웃/리다이렉트:
    await axios.post('/api/auth/token/logout', {}, { withCredentials: true })
    location.replace('/login')
  } catch (e) {
    const s = e?.response?.status
    if (s === 403) alert('접근 권한이 없습니다.')
    else alert(e?.response?.data || '위임 실패')
  } finally {
    delegating.value = false
  }
}

// ===== IP 화이트리스트 =====
const ipList = ref([])
const ipLoading = ref(false)

const ipEditing = ref(false)    // 편집 모드
const ipSaving  = ref(false)

const newIp = ref('')
const newIpMemo = ref('')
const ipFormError = ref('')

const editingRowId = ref(null)  // 행 편집 ID
const editingRow = ref({
  ipAddress: '',
  memo: '',
  isActive: 'Y',
})

const canAddIp = computed(() => {
  const ip = (newIp.value || '').trim()
  if (!ip) return false
  return validateIpFormat(ip)
})

// 검색어: input에 타이핑용 / 실제 필터용 분리
const ipSearchInput = ref('')
const ipSearch = ref('')

const filteredIpList = computed(() => {
  const list = ipList.value || []
  const raw = ipSearch.value || ''
  const q = raw.trim().toLowerCase()
  if (!q) return list

  return list.filter((row) => {
    const ip = String(row.ipAddress || '').toLowerCase()
    const memo = String(row.memo || '').toLowerCase()
    const dt = String(row.updatedAt || row.createdAt || '').toLowerCase()
    return ip.includes(q) || memo.includes(q) || dt.includes(q)
  })
})

function applyIpSearch() {
  ipSearch.value = (ipSearchInput.value || '').trim()
}

function clearIpSearch() {
  ipSearchInput.value = ''
  ipSearch.value = ''
}

function splitDateTime(str) {
  if (!str) return ['', '']
  const s = String(str).replace('T', ' ')
  const [d, t] = s.split(' ')
  return [d || '', t || '']
}

// IPv4 검사
function isValidIPv4(ip) {
  const v = (ip || '').trim()
  const v4 =
      /^(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)){3}$/
  return v4.test(v)
}

// IPv4 → 두 개의 헥스텟으로 변환
function ipv4ToHextets(ip) {
  const parts = ip.split('.').map(n => Number(n))
  if (parts.length !== 4 || parts.some(n => !Number.isInteger(n) || n < 0 || n > 255)) return null
  const hi = (parts[0] << 8) + parts[1]
  const lo = (parts[2] << 8) + parts[3]
  return [hi.toString(16), lo.toString(16)]
}

// 순수 IPv6 검사
function isValidPureIPv6(ip) {
  const v = (ip || '').trim()
  if (!v) return false
  const isValidBlock = (s) => /^[0-9a-fA-F]{1,4}$/.test(s)
  const hasDoubleColon = v.includes('::')
  if (hasDoubleColon) {
    if (v.indexOf('::') !== v.lastIndexOf('::')) return false
    const parts = v.split('::')
    if (parts.length !== 2) return false
    const head = parts[0] ? parts[0].split(':') : []
    const tail = parts[1] ? parts[1].split(':') : []
    if (head.some(g => !isValidBlock(g)) || tail.some(g => !isValidBlock(g))) return false
    const numGroups = head.length + tail.length
    return numGroups < 8
  } else {
    const groups = v.split(':')
    if (groups.length !== 8) return false
    return groups.every(isValidBlock)
  }
}

// 최종 IP 형식 검사
function validateIpFormat(ip) {
  const v = (ip || '').trim()
  if (!v) return false
  if (!v.includes('.')) {
    if (isValidIPv4(v)) return true
    return isValidPureIPv6(v)
  }
  if (isValidIPv4(v)) return true
  const lastColon = v.lastIndexOf(':')
  if (lastColon === -1) return false
  const ipv6Part = v.slice(0, lastColon)
  const ipv4Part = v.slice(lastColon + 1)
  if (!isValidIPv4(ipv4Part)) return false
  const hextets = ipv4ToHextets(ipv4Part)
  if (!hextets) return false
  const reconstructed = `${ipv6Part}:${hextets[0]}:${hextets[1]}`
  return isValidPureIPv6(reconstructed)
}

// 새 IP 입력 감시
watch(newIp, (val) => {
  const ip = (val || '').trim()
  if (!ip) {
    ipFormError.value = ''
    return
  }
  ipFormError.value = validateIpFormat(ip) ? '' : '올바른 IPv4/IPv6 형식이 아닙니다.'
})

// IP 목록 조회
async function fetchIpWhitelist() {
  if (!isSuperEmail.value) return
  try {
    ipLoading.value = true
    const { data } = await axios.get('/api/me/ip-whitelist', { withCredentials: true })
    ipList.value = Array.isArray(data) ? data : []
  } catch (e) {
    alert(e?.response?.data || 'IP 목록을 불러오지 못했습니다.')
  } finally {
    ipLoading.value = false
  }
}

// 새 IP 추가
async function addIp() {
  const ip   = (newIp.value || '').trim()
  const memo = (newIpMemo.value || '').trim()
  if (!ip) {
    ipFormError.value = 'IP 주소를 입력하세요.'
    return
  }
  if (!validateIpFormat(ip)) {
    ipFormError.value = '올바른 IPv4/IPv6 형식이 아닙니다.'
    return
  }
  try {
    ipSaving.value = true
    await axios.post(
        '/api/me/ip-whitelist',
        {
          ipId: null,
          ipAddress: ip,
          memo: memo || null,
          isActive: 'Y',
        },
        { withCredentials: true },
    )
    newIp.value = ''
    newIpMemo.value = ''
    ipFormError.value = ''
    await fetchIpWhitelist()
  } catch (e) {
    alert(e?.response?.data || 'IP 추가에 실패했습니다.')
  } finally {
    ipSaving.value = false
  }
}

// 행 편집 시작
function startIpRowEdit(row) {
  if (!ipEditing.value) return
  editingRowId.value = row.ipId
  editingRow.value = {
    ipAddress: row.ipAddress || '',
    memo: row.memo || '',
    isActive: row.isActive || 'Y',
  }
}

// 행 편집 취소
function cancelIpRowEdit() {
  editingRowId.value = null
  editingRow.value = { ipAddress: '', memo: '', isActive: 'Y' }
}

// 행 저장
async function saveIpRow(row) {
  const ip   = (editingRow.value.ipAddress || '').trim()
  const memo = (editingRow.value.memo || '').trim()
  const next = editingRow.value.isActive || 'Y'
  if (!ip) {
    alert('IP 주소를 입력하세요.')
    return
  }
  if (!validateIpFormat(ip)) {
    alert('올바른 IPv4/IPv6 형식이 아닙니다.')
    return
  }
  try {
    ipSaving.value = true
    await axios.put(
        `/api/me/ip-whitelist/${row.ipId}`,
        {
          ipAddress: ip,
          memo: memo || null,
          isActive: next,
        },
        { withCredentials: true },
    )
    cancelIpRowEdit()
    await fetchIpWhitelist()
  } catch (e) {
    alert(e?.response?.data || 'IP 수정에 실패했습니다.')
  } finally {
    ipSaving.value = false
  }
}

// 행 삭제
async function deleteIpRow(row) {
  if (!row || !row.ipId) return
  if (!confirm('이 IP를 삭제하시겠습니까?')) return

  try {
    ipSaving.value = true
    await axios.delete(`/api/me/ip-whitelist/${row.ipId}`, { withCredentials: true })
    ipList.value = (ipList.value || []).filter(r => r.ipId !== row.ipId)
    if (editingRowId.value === row.ipId) {
      cancelIpRowEdit()
    }
  } catch (e) {
    alert(e?.response?.data || 'IP 삭제에 실패했습니다.')
  } finally {
    ipSaving.value = false
  }
}

// 전체 수정 취소
function onIpCancel() {
  ipEditing.value  = false
  newIp.value      = ''
  newIpMemo.value  = ''
  ipFormError.value = ''
  cancelIpRowEdit()
}

// 특별계정 켜지면 센터 목록 + IP 화이트리스트 불러오기
watch(
    () => isSuperEmail.value,
    async (ok) => {
      if (!ok) return
      await Promise.all([
        fetchCenters(),
        fetchIpWhitelist(),
      ])
    },
    { immediate: true },
)

// 라우트 이탈 시 초기화
onBeforeRouteLeave(() => { onDelegateCancel() })
</script>

<style>
.fade-enter-active, .fade-leave-active { transition: opacity .15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 다크모드에서 네이티브 컨트롤(스피너 포함)을 다크 스킨으로 */
.dark .spin-dark { color-scheme: dark; }
.dark .spin-dark::-webkit-inner-spin-button,
.dark .spin-dark::-webkit-outer-spin-button {
  filter: invert(2) brightness(0.5) contrast(1.6) opacity(.2);
}

/* 슬라이드 전환 */
.slide-enter-active, .slide-leave-active {
  transition: max-height .28s ease, opacity .2s ease;
}
.slide-enter-from, .slide-leave-to {
  max-height: 0;
  opacity: 0;
}
.slide-enter-to, .slide-leave-from {
  max-height: 2000px;
  opacity: 1;
}

.ip-scroll { scrollbar-gutter: stable; }
</style>
