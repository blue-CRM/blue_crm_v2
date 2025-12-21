<template>
  <AdminLayout>
    <PageBreadcrumb pageTitle="내방일정 스케쥴표" />

    <div class="grid grid-cols-12 gap-4 min-w-0">
      <div class="col-span-12 space-y-6 min-w-0">
        <div
            class="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
        >
          <!-- 상단 툴바 -->
          <div
              class="flex items-center justify-between gap-2 px-4 py-3 border-b border-gray-200 dark:border-gray-800 bg-white/80 dark:bg-gray-900/60"
          >
            <div class="flex items-center gap-2">
              <button
                  :class="btnSoft"
                  @click="goPrevWeek"
              >
                < 이전주
              </button>
              <button
                  :class="btnSoft"
                  @click="goToday"
              >
                오늘
              </button>
              <button
                  :class="btnSoft"
                  @click="goNextWeek"
              >
                다음주 >
              </button>
            </div>

            <div class="font-semibold text-gray-700 dark:text-white/70">
              {{ weekTitle }}
            </div>

            <div class="flex items-center gap-2">
              <button
                  v-if="canWrite"
                  :class="btnSoftPrimary"
                  @click="openManualAdd"
              >
                일정 추가 +
              </button>

              <button
                  :disabled="loadingSchedules"
                  @click="onRefreshSchedules"
                  title="새로고침"
                  class="inline-flex h-9 w-9 items-center justify-center rounded-full
                   border border-gray-200 bg-white text-gray-600 hover:bg-gray-100
                   disabled:opacity-60 disabled:cursor-not-allowed
                   focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-200
                   dark:border-gray-700 dark:bg-white/5 dark:text-gray-400 dark:hover:bg-gray-800 dark:focus-visible:ring-blue-900"
              >
                <RefreshIcon :class="['h-4 w-4', loadingSchedules ? 'animate-spin' : '']" />
              </button>

            </div>
          </div>

          <!-- 스크롤 호스트 -->
          <div
              ref="scrollHost"
              class="relative max-w-full overflow-x-auto overflow-y-auto custom-scrollbar"
              :style="{ maxHeight: scrollMaxHeight }"
          >
            <!-- 전체 그리드 -->
            <div class="min-w-max" :style="{ width: totalWidth + 'px' }">
              <!-- 헤더 (sticky) -->
              <div class="sticky top-0 z-40 bg-white dark:bg-gray-900">
                <!-- 날짜 그룹 헤더 -->
                <div
                    class="grid border-b border-gray-200 dark:border-gray-800"
                    :style="{ gridTemplateColumns: gridTemplateColumns }"
                >
                  <div
                      class="sticky left-0 z-50 flex items-center justify-center h-10 border-r border-gray-200
                           bg-slate-50 text-slate-700 text-xs font-semibold dark:bg-gray-900/60 dark:text-slate-200"
                  >
                    시간
                  </div>

                  <template v-for="(d, dayIdx) in days" :key="d.key">
                    <div
                        class="h-10 flex items-center justify-center text-sm font-semibold"
                        :style="{ gridColumn: `span ${roomsPerDay}` }"
                        :class="[
                        dayHeaderClass(d.key),
                        dayIdx === days.length - 1 ? '' : 'border-r-2 border-gray-300 dark:border-gray-700'
                      ]"
                    >
                      {{ d.label }}
                    </div>
                  </template>
                </div>

                <!-- 룸 헤더 -->
                <div
                    class="grid border-b border-gray-200 dark:border-gray-800"
                    :style="{ gridTemplateColumns: gridTemplateColumns }"
                >
                  <div
                      class="sticky left-0 z-50 h-9 border-r border-gray-200
                           bg-slate-200 dark:bg-slate-800 dark:border-gray-800"
                  />

                  <template v-for="(col, colIdx) in columns" :key="col.key">
                    <div
                        class="h-9 flex items-center justify-center text-xs font-medium border-r border-gray-200 dark:border-gray-800"
                        :class="[
                        roomHeaderClass(col.dayKey),
                        col.isDayEnd ? 'border-r-2 border-gray-300 dark:border-gray-700' : ''
                      ]"
                    >
                      {{ col.roomLabel }}
                    </div>
                  </template>
                </div>
              </div>

              <!-- 바디 -->
              <div class="relative">
                <!-- 선택 드래그 오버레이 -->
                <div
                    v-if="selectionVisible"
                    class="absolute inset-y-0 right-0 z-20 pointer-events-none"
                    :style="{ left: TIME_COL_WIDTH + 'px', width: colCount * COL_WIDTH + 'px' }"
                >
                  <div class="absolute" :style="selectionRectStyle">
                    <div
                        class="h-full w-full rounded-xl border border-blue-500/60 bg-blue-500/10
                             ring-2 ring-blue-400/15 shadow-[0_6px_16px_rgba(37,99,235,0.10)]
                             backdrop-blur-[1px]"
                    />
                  </div>
                </div>

                <!-- 이벤트 레이어: 타임컬럼 제외 -->
                <div
                    class="absolute inset-y-0 z-30 pointer-events-none"
                    :style="{ left: TIME_COL_WIDTH + 'px', width: (colCount * COL_WIDTH) + 'px' }"
                >
                  <div
                      v-for="ev in visibleEvents"
                      :key="ev.id"
                      class="absolute pointer-events-auto select-none"
                      :style="eventStyle(ev)"
                      @pointerdown.stop.prevent="onEventPointerDown($event, ev)"
                      @click.stop="onEventClick(ev)"
                  >
                    <!-- 위 리사이즈 핸들 -->
                    <div
                        v-if="canResize(ev)"
                        class="absolute left-0 right-0 top-0 h-2 cursor-ns-resize"
                        @pointerdown.stop.prevent="onResizeDown($event, ev, 'start')"
                        @click.stop.prevent
                    />
                    <!-- 아래 리사이즈 핸들 -->
                    <div
                        v-if="canResize(ev)"
                        class="absolute left-0 right-0 bottom-0 h-2 cursor-ns-resize"
                        @pointerdown.stop.prevent="onResizeDown($event, ev, 'end')"
                        @click.stop.prevent
                    />
                    <!-- 본문 -->
                    <div
                        class="h-full w-full rounded-md shadow-sm border text-xs flex flex-col justify-between overflow-hidden"
                        :class="eventClass(ev)"
                    >
                      <div class="flex items-start justify-between gap-2 px-2 py-1">
                        <div class="min-w-0">
                          <div class="font-semibold truncate">
                            {{ ev.scheduledByName }}
                          </div>
                          <div class="text-[11px] opacity-80">
                            {{ ev.customerName }}
                          </div>
                        </div>

                        <button
                            v-if="canEditEvent(ev)"
                            class="shrink-0 w-5 h-5 rounded hover:bg-black/10 dark:hover:bg-white/10 flex items-center justify-center"
                            @click.stop="deleteEvent(ev.id)"
                            title="삭제"
                        >
                          ✕
                        </button>
                      </div>

                      <div class="px-2 pb-1 text-[11px] opacity-80">
                        {{ ev.dayLabel }} · {{ timeLabel(ev.startSlot) }} - {{ timeLabel(ev.endSlot) }} · {{ ev.room }}룸
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 행들 -->
                <div
                    v-for="slotIdx in slots"
                    :key="slotIdx"
                    class="grid"
                    :style="{ gridTemplateColumns: gridTemplateColumns, height: ROW_HEIGHT + 'px' }"
                >
                  <!-- 시간(좌측 sticky) -->
                  <div
                      class="sticky left-0 z-40 flex items-center justify-end pr-2 text-xs border-r border-gray-200 dark:border-gray-800"
                      :class="timeColBgClass(slotIdx)"
                      :style="rowTopBorderStyle(slotIdx)"
                  >
                    {{ timeLabel(slotIdx) }}
                  </div>

                  <!-- 셀들 -->
                  <template v-for="(col, colIdx) in columns" :key="col.key">
                    <div
                        class="border-r border-gray-200 dark:border-gray-800"
                        data-cal-cell="1"
                        :data-col="colIdx"
                        :data-slot="slotIdx"
                        :class="[
                        cellBgClass(slotIdx, col.dayKey),
                        col.isDayEnd ? 'border-r-2 border-gray-300 dark:border-gray-700' : ''
                      ]"
                        :style="rowTopBorderStyle(slotIdx)"
                        @pointerdown.stop.prevent="onCellPointerDown($event, colIdx, slotIdx)"
                    />
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- 이벤트 모달 -->
          <div
              v-if="isOpen"
              ref="modalRoot"
              class="fixed inset-0 z-[100000] flex items-center justify-center bg-black/40 p-4"
              tabindex="0"
              @click.self="closeModal"
              @keydown.esc.prevent.stop="closeModal"
          >
            <div class="w-full max-w-[700px] rounded-2xl bg-white dark:bg-gray-900 shadow-xl border border-gray-200 dark:border-gray-800">
              <!-- 헤더 -->
              <div class="px-5 py-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
                <h3 class="text-base font-semibold text-gray-800 dark:text-gray-100">
                  {{ editingEventId ? '내방 일정 수정' : '내방 일정 추가' }}
                </h3>
                <button class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300" @click="closeModal">✕</button>
              </div>

              <!-- 바디 -->
              <div class="px-5 py-4 space-y-4">
                <!-- 시간/날짜 한 줄 -->
                <div class="rounded-xl border border-gray-200 p-3 text-sm text-gray-700 dark:border-gray-800 dark:text-gray-200">
                  일정: <b>{{ pendingDateTimeLabel }}</b>
                </div>

                <!-- 회의실 선택 (API 로드) -->
                <div class="space-y-1">
                  <label class="block text-sm text-gray-600 dark:text-gray-300">회의실</label>
                  <select
                      v-model.number="pendingRoom"
                      :disabled="!modalCanEdit"
                      class="w-full h-11 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2
                 text-sm focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                 dark:bg-gray-800 dark:text-gray-200"
                  >
                    <option :value="null" disabled>회의실을 선택하세요</option>
                    <option v-for="r in roomListForGrid" :key="r.room" :value="r.room">
                      {{ r.label }}
                    </option>
                  </select>
                </div>

                <div class="rounded-xl border border-gray-200 p-3 text-sm text-gray-700 dark:border-gray-800 dark:text-gray-200">
                  일정 등록자: <b>{{ editingScheduledByName }}</b>
                </div>

                <!-- 고객 -->
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <label class="block text-sm text-gray-600 dark:text-gray-300">고객</label>

                    <button
                        v-if="modalCanEdit && !isCustomerLocked && pickedCustomer"
                        class="text-xs px-2 py-1 rounded-md border border-gray-200 hover:bg-gray-50
                   dark:border-gray-700 dark:hover:bg-gray-800/70"
                        @click="clearPickedCustomer"
                    >
                      선택 해제
                    </button>
                  </div>

                  <!-- 고정 고객(쿼리 customerId) 또는 선택된 고객 표시 -->
                  <div v-if="pickedCustomer" class="rounded-xl border border-gray-200 p-3 dark:border-gray-800">
                    <div class="flex items-center justify-between gap-3">
                      <div class="min-w-0">
                        <div class="text-sm text-gray-800 dark:text-gray-100 truncate">
                          <b>{{ pickedCustomer.customerName }}</b>
                          <span class="text-gray-400 ml-2">{{ pickedCustomer.customerPhone }}</span>
                        </div>
                        <div class="text-xs text-gray-500 dark:text-gray-400">
                          생성일: {{ pickedCustomer.customerCreatedAt }}
                        </div>
                      </div>

                      <span v-if="isCustomerLocked" class="text-[11px] px-2 py-1 rounded-md bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-200">
                      고정
                    </span>
                    </div>
                  </div>

                  <!-- customerId 없을 때만: 고객 검색/선택 -->
                  <template v-if="!isCustomerLocked">
                    <div class="flex gap-2">
                      <input
                          v-model.trim="customerQuery"
                          :disabled="!modalCanEdit"
                          @input="debouncedCustomerSearch"
                          type="text"
                          placeholder="이름 또는 전화번호로 검색"
                          class="h-11 w-full rounded-lg border px-3
                     bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                      />
                      <button
                          @click="searchCustomers"
                          :disabled="submitting || !modalCanEdit"
                          class="inline-flex h-11 items-center justify-center rounded-lg border border-gray-200 px-4 text-sm
                     hover:bg-gray-50 disabled:opacity-60
                     dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:hover:bg-gray-700/60"
                      >
                        검색
                      </button>
                    </div>

                    <p class="text-xs text-gray-500 dark:text-gray-400">
                      내방 상태이며 예약이 없는 고객만 표시됩니다.
                    </p>

                    <div
                        v-if="customerResults.length"
                        class="max-h-52 overflow-auto border border-gray-100 dark:border-gray-800 rounded-md"
                    >
                      <ul>
                        <li
                            v-for="c in customerResults"
                            :key="c.customerId"
                            @click="selectCustomer(c)"
                            class="flex items-center justify-between px-3 py-2 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800"
                            :class="pickedCustomer?.customerId === c.customerId ? 'bg-gray-100 dark:bg-gray-800/70' : ''"
                        >
                          <div class="min-w-0">
                            <div class="text-sm text-gray-700 dark:text-gray-200 truncate">
                              <b>{{ c.customerName }}</b>
                              <span class="text-gray-400 ml-2">{{ c.customerPhone }}</span>
                            </div>
                            <div class="text-xs text-gray-500 dark:text-gray-400">
                              생성일: {{ c.customerCreatedAt }}
                            </div>
                          </div>

                          <button
                              class="px-2 py-1 text-xs rounded-md border transition"
                              @click.stop="selectCustomer(c)"
                              :class="pickedCustomer?.customerId === c.customerId
                    ? 'bg-gray-900 text-white border-gray-900 hover:bg-gray-800 dark:bg-white dark:text-gray-900 dark:border-white dark:hover:bg-gray-200'
                    : 'text-gray-700 border-gray-300 hover:bg-gray-50 dark:text-gray-300 dark:border-gray-700 dark:hover:bg-gray-800/70'"
                          >
                            {{ pickedCustomer?.customerId === c.customerId ? '선택됨' : '선택' }}
                          </button>
                        </li>
                      </ul>
                    </div>

                    <div v-else class="text-xs text-gray-400 dark:text-gray-500">
                      검색 결과가 없습니다.
                    </div>
                  </template>

                  <!-- customerId 있는데 아직 로드 전 -->
                  <div v-if="isCustomerLocked && !pickedCustomer" class="text-xs text-gray-500 dark:text-gray-400">
                    고객 정보를 불러오는 중...
                  </div>
                </div>

                <!-- 기타 회의실일 때만 메모 등록 -->
                <div v-if="isEtcSelected" class="space-y-1">
                  <label class="block text-sm text-gray-600 dark:text-gray-300" :disabled="!modalCanEdit">메모 (기타 필수)</label>
                  <textarea
                      v-model.trim="memo"
                      rows="3"
                      class="w-full rounded-lg border px-3 py-2
                         bg-white text-gray-800 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10
                         dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
                      placeholder="기타 회의실 예약 사유를 입력하세요"
                  />
                </div>
              </div>

              <!-- 푸터 -->
              <div class="px-5 py-3 border-t border-gray-200 dark:border-gray-800 flex items-center justify-end gap-2">
                <button
                    class="h-10 px-4 rounded-lg border border-gray-200 text-sm hover:bg-gray-50
               dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:hover:bg-gray-700/60"
                    @click="closeModal"
                >
                  취소
                </button>

                <button
                    class="h-10 px-4 rounded-lg bg-blue-600 text-white text-sm disabled:opacity-60"
                    :disabled="submitting || !canSave || !modalCanEdit"
                    @click="saveEvent"
                >
                  {{ submitting ? '처리 중...' : (editingEventId ? '수정 저장' : '추가') }}
                </button>

                <button
                    v-if="editingEventId"
                    class="h-10 px-4 rounded-lg border border-red-500 bg-red-500 text-white text-sm hover:bg-red-600 disabled:opacity-60"
                    :disabled="submitting || !modalCanEdit"
                    @click="deleteEvent(editingEventId)"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import PageBreadcrumb from '@/components/common/PageBreadcrumb.vue'
import axios from '@/plugins/axios'
import { useAuthStore } from "@/stores/auth.js";
import { useRoute } from 'vue-router'
import {RefreshIcon} from "@/icons";

const myUserId = ref<number | null>(null)

async function loadMyId() {
  try {
    const { data } = await axios.get('/api/me', { withCredentials: true })
    myUserId.value = data.userId ?? null
    myUserName.value = data.userName ?? ''
  } catch {
    myUserId.value = null
    myUserName.value = ''
  }
}

function findEventById(id: string | null) {
  if (!id) return null
  return events.value.find(e => e.id === id) ?? null
}
const modalCanEdit = computed(() => {
  // 신규 등록: 역할만 체크
  if (!editingEventId.value) return canWrite.value

  // 수정 모달: “내 이벤트”만 true
  const ev = findEventById(editingEventId.value)
  return !!ev && canEditEvent(ev)
})

const auth = useAuthStore();
const route = useRoute()
const role = computed(() => auth.grants.role)
const canWrite = computed(() => role.value === 'MANAGER' || role.value === 'STAFF')
const myUserName = ref<string>('')
function canEditEvent(ev: ScheduleEvent) {
  return canWrite.value && myUserId.value != null && ev.scheduledByUserId === myUserId.value
}

// 버튼 색상
const btnSoft =
    'h-9 px-3 rounded-xl border text-sm font-medium transition ' +
    'border-gray-200 bg-white text-gray-700 shadow-sm ' +
    'hover:bg-gray-50 hover:border-gray-300 ' +
    'focus:outline-none focus:ring-2 focus:ring-blue-500/20 ' +
    'dark:border-gray-800 dark:bg-gray-900/40 dark:text-gray-200 dark:shadow-none ' +
    'dark:hover:bg-white/[0.03]'

const btnSoftPrimary =
    'h-9 px-3 rounded-xl text-sm font-semibold transition ' +
    'bg-blue-600 text-white shadow-sm ' +
    'hover:bg-blue-700 ' +
    'focus:outline-none focus:ring-2 focus:ring-blue-500/30 ' +
    'dark:bg-blue-600 dark:hover:bg-blue-500'

/** ===== 설정 ===== */
const ROW_HEIGHT = 40
const TIME_COL_WIDTH = 100
const COL_WIDTH = 130

const START_HOUR = 8
const SLOT_MINUTES = 30

const AUTO_SCROLL_MARGIN = 60
const AUTO_SCROLL_MIN = 2
const AUTO_SCROLL_MAX = 14
const AUTO_SCROLL_EASE = 0.25

const START_MINUTES = START_HOUR * 60
const END_MINUTES_EXCLUSIVE = (22 * 60) + 30
const slotCount = (END_MINUTES_EXCLUSIVE - START_MINUTES) / SLOT_MINUTES

const booting = ref(true)
const focusDayKey = ref<string | null>(null)
const shouldAutoOpenFocus = ref(false)

const memo = ref('')

type RoomOption = { room: number; label: string }
const rooms = ref<RoomOption[]>([])
const fallbackRooms: RoomOption[] = Array.from({ length: 5 }, (_, i) => ({ room: i + 1, label: `${i + 1}룸` }))

const roomListForGrid = computed<RoomOption[]>(() => {
  return rooms.value.length ? rooms.value : fallbackRooms
})
const roomsPerDay = computed(() => roomListForGrid.value.length)

/** ===== 날짜 유틸 ===== */
function pad2(n: number) { return String(n).padStart(2, '0') }
function ymd(d: Date) { return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` }
function addDays(d: Date, days: number) { const x = new Date(d); x.setDate(x.getDate() + days); return x }
function startOfWeek(d: Date) {
  const x = new Date(d)
  const day = x.getDay() // 0=일
  const diff = (day === 0 ? -6 : 1 - day) // 월요일 시작
  x.setDate(x.getDate() + diff)
  x.setHours(0, 0, 0, 0)
  return x
}
function koDow(d: Date) { return ['일', '월', '화', '수', '목', '금', '토'][d.getDay()] }

/** ===== 상태 ===== */
const todayKey = ref(ymd(new Date()))
const weekStart = ref(startOfWeek(new Date()))

const days = computed(() => {
  const out: Array<{ key: string; date: Date; label: string }> = []
  for (let i = 0; i < 7; i++) {
    const dt = addDays(weekStart.value, i)
    out.push({
      key: ymd(dt),
      date: dt,
      label: `${pad2(dt.getMonth() + 1)}.${pad2(dt.getDate())} ${koDow(dt)}`
    })
  }
  return out
})

type Column = { key: string; dayKey: string; dayLabel: string; room: number; roomLabel: string; isDayEnd: boolean }
const columns = computed<Column[]>(() => {
  const out: Column[] = []
  const perDay = roomsPerDay.value
  for (const d of days.value) {
    roomListForGrid.value.forEach((r, idx) => {
      out.push({
        key: `${d.key}#${r.room}`,
        dayKey: d.key,
        dayLabel: d.label,
        room: r.room, // DB roomId
        roomLabel: r.label, // DB roomName
        isDayEnd: idx === perDay - 1
      })
    })
  }
  return out
})

const colCount = computed(() => columns.value.length)
const totalWidth = computed(() => TIME_COL_WIDTH + colCount.value * COL_WIDTH)
const gridTemplateColumns = computed(() => `${TIME_COL_WIDTH}px repeat(${colCount.value}, ${COL_WIDTH}px)`)

const weekTitle = computed(() => {
  const s = days.value[0]?.date
  const e = days.value[6]?.date
  if (!s || !e) return ''
  return `${s.getFullYear()}.${pad2(s.getMonth()+1)}.${pad2(s.getDate())} - ${e.getFullYear()}.${pad2(e.getMonth()+1)}.${pad2(e.getDate())}`
})

const slots = computed(() => Array.from({ length: slotCount }, (_, i) => i))

/** ===== 색/배경 ===== */
function dayHeaderClass(dayKey: string) {
  return dayKey === todayKey.value
      ? 'bg-amber-100/80 text-amber-900 dark:bg-amber-900/35 dark:text-amber-200' // 오늘
      : 'bg-blue-100/80 text-blue-900 dark:bg-blue-900/35 dark:text-blue-200' // 다른날
}
function roomHeaderClass(dayKey: string) {
  return dayKey === todayKey.value
      ? 'bg-amber-50 text-amber-900 dark:bg-amber-900/20 dark:text-amber-200' // 오늘
      : 'bg-blue-50 text-blue-900 dark:bg-blue-900/20 dark:text-blue-200' // 다른날
}
function cellBgClass(slotIdx: number, dayKey: string) {
  const isZebraEven = slotIdx % 2 === 0
  return isZebraEven ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-800/60'
  // if (dayKey !== todayKey.value) {
  //   return isZebraEven ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-800/60'
  // }
  // return isZebraEven ? 'bg-amber-50/80 dark:bg-amber-900/15' : 'bg-amber-100/60 dark:bg-amber-900/25'
}
function timeColBgClass(slotIdx: number) {
  return slotIdx % 2 === 0
      ? 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
      : 'bg-slate-200 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
}

/** ===== 스크롤 높이 + Ctrl+휠 가로스크롤 ===== */
const scrollHost = ref<HTMLElement | null>(null)
const scrollMaxHeight = ref('520px')

function updateScrollHeight() {
  const el = scrollHost.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const viewportHeight = window.innerHeight || document.documentElement.clientHeight
  const bottomPadding = 40
  let h = viewportHeight - rect.top - bottomPadding
  if (h < 260) h = 260
  scrollMaxHeight.value = `${h}px`
}

function handleHorizontalWheel(e: WheelEvent) {
  const el = scrollHost.value
  if (!el) return
  if (!e.ctrlKey) return
  e.preventDefault()
  const base = e.deltaY !== 0 ? e.deltaY : e.deltaX
  const factor = (e as any).deltaMode === 1 ? 20 : 1
  el.scrollLeft += base * factor
}

function scrollToDay(dayKey: string) {
  const host = scrollHost.value
  if (!host) return

  const idx = days.value.findIndex(d => d.key === dayKey)
  if (idx < 0) { host.scrollLeft = 0; return }

  const target = idx * roomsPerDay.value * COL_WIDTH
  host.scrollLeft = target
}
function scrollToEventVertical(ev: ScheduleEvent) {
  const host = scrollHost.value
  if (!host) return

  const paddingTop = 80 // sticky header 여유
  const targetTop = ev.startSlot * ROW_HEIGHT

  host.scrollTop = Math.max(0, targetTop - paddingTop)
}

onMounted(async () => {
  await loadMyId()
  await loadRooms()
  await loadFocusIfLocked()
  await loadSchedules()
  updateScrollHeight()
  window.addEventListener('resize', updateScrollHeight)
  window.addEventListener('orientationchange', updateScrollHeight)

  const host = scrollHost.value
  if (host) host.addEventListener('wheel', handleHorizontalWheel, {passive: false})

  // 스크롤 대상: focusDayKey > (이번주면 오늘) > 주 시작
  await nextTick()
  const target =
      (focusDayKey.value && days.value.some(d => d.key === focusDayKey.value)) ? focusDayKey.value
          : (days.value.some(d => d.key === todayKey.value) ? todayKey.value : days.value[0]?.key)

  if (target) {
    requestAnimationFrame(() => {
      scrollToDay(target)

      // ⬇ customerId로 포커스 들어온 경우만 세로 스크롤
      if (lockedCustomerId.value) {
        const ev = events.value.find(e => e.customerId === lockedCustomerId.value)
        if (ev) scrollToEventVertical(ev)
      }
    })
  }

  booting.value = false
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateScrollHeight)
  window.removeEventListener('orientationchange', updateScrollHeight)
  const host = scrollHost.value
  if (host) host.removeEventListener('wheel', handleHorizontalWheel as any)
})

watch(weekStart, async () => {
  if (booting.value) return

  await nextTick()
  await loadSchedules()

  const target =
      (focusDayKey.value && days.value.some(d => d.key === focusDayKey.value)) ? focusDayKey.value
          : (days.value.some(d => d.key === todayKey.value) ? todayKey.value : days.value[0]?.key)

  if (target) {
    requestAnimationFrame(() => {
      scrollToDay(target)

      // ⬇ customerId로 포커스 들어온 경우만 세로 스크롤
      if (lockedCustomerId.value) {
        const ev = events.value.find(e => e.customerId === lockedCustomerId.value)
        if (ev) scrollToEventVertical(ev)
      }
    })
  }
})

/** ===== 시간 라벨 / 정오 라인 ===== */
function timeLabel(slotIdx: number) {
  const totalMin = START_MINUTES + slotIdx * SLOT_MINUTES
  const hh = Math.floor(totalMin / 60)
  const mm = totalMin % 60
  const ap = hh < 12 ? '오전' : '오후'
  const hh12 = hh % 12 === 0 ? 12 : hh % 12
  return `${ap} ${hh12}시${mm === 0 ? '' : ' 30분'}`
}
function rowTopBorderStyle(slotIdx: number) {
  const noonSlot = ((13 * 60) - START_MINUTES) / SLOT_MINUTES
  if (slotIdx === noonSlot) {
    return { borderTop: '2px solid rgb(209 213 219)' }
  }
  return {}
}

/** ===== 선택 드래그 (가로+세로 스윕) ===== */
const selecting = ref(false)
const selection = reactive({ colFrom: -1, colTo: -1, slotFrom: -1, slotTo: -1 })

function normalizeSelection() {
  const c1 = selection.colFrom   // 고정 (가로 확장 금지)
  const c2 = selection.colFrom   // 고정 (가로 확장 금지)
  const s1 = Math.min(selection.slotFrom, selection.slotTo)
  const s2 = Math.max(selection.slotFrom, selection.slotTo)
  return { c1, c2, s1, s2 }
}

function isSelected(colIdx: number, slotIdx: number) {
  if (!selecting.value && selection.colFrom < 0) return false
  const { c1, c2, s1, s2 } = normalizeSelection()
  return colIdx >= c1 && colIdx <= c2 && slotIdx >= s1 && slotIdx <= s2
}

function clearSelection() {
  selecting.value = false
  selection.colFrom = -1
  selection.colTo = -1
  selection.slotFrom = -1
  selection.slotTo = -1

  window.removeEventListener('pointermove', onSelectingPointerMove)
}

const selectionVisible = computed(() => selection.colFrom >= 0 && selection.slotFrom >= 0)

const selectionRectStyle = computed(() => {
  if (!selectionVisible.value) return {}
  const { c1, s1, s2 } = normalizeSelection()
  return {
    left: `${c1 * COL_WIDTH}px`,
    top: `${s1 * ROW_HEIGHT}px`,
    width: `${COL_WIDTH}px`,
    height: `${(s2 - s1 + 1) * ROW_HEIGHT}px`
  }
})

function onCellPointerDown(_e: PointerEvent, colIdx: number, slotIdx: number) {
  if (!canWrite.value) return

  selecting.value = true
  selection.colFrom = colIdx
  selection.colTo = colIdx
  selection.slotFrom = slotIdx
  selection.slotTo = slotIdx

  window.addEventListener('pointermove', onSelectingPointerMove, { passive: true })
  window.addEventListener('pointerup', onSelectingPointerUp, { once: true })
}
function onSelectingPointerMove(e: PointerEvent) {
  if (!selecting.value) return
  const el = document.elementFromPoint(e.clientX, e.clientY) as HTMLElement | null
  const cell = el?.closest?.('[data-cal-cell="1"]') as HTMLElement | null
  if (!cell) return

  const s = Number(cell.dataset.slot)
  if (!Number.isFinite(s)) return

  // 가로는 고정, 세로만 변화
  selection.colTo = selection.colFrom
  selection.slotTo = s
}
function onSelectingPointerUp() {
  if (!selecting.value) return

  // 선택 범위 먼저 확보
  const range = normalizeSelection()

  // 오버레이 즉시 제거
  clearSelection()

  // 모달 오픈 (range로)
  openModalFromSelection(range)
}

/** ===== 이벤트 모델 ===== */
const lockedCustomerId = computed(() => {
  const raw = route.query.customerId
  const v = Array.isArray(raw) ? raw[0] : raw
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : null
})
const isCustomerLocked = computed(() => lockedCustomerId.value != null)

type CustomerPick = {
  customerId: number
  customerName: string
  customerPhone: string
  customerCreatedAt: string
}
const customerQuery = ref('')
const customerResults = ref<CustomerPick[]>([])
const pickedCustomer = ref<CustomerPick | null>(null)

const submitting = ref(false)
const modalRoot = ref<HTMLElement | null>(null)

const pendingDayKey = ref<string>('')     // 선택된 날짜 key (YYYY-MM-DD)
const pendingDayLabel = ref<string>('')   // 선택된 라벨 (MM.DD 요일)
const pendingSlot = ref<number>(0)        // 선택된 슬롯 (startSlot)
const pendingRoom = ref<number | null>(null)

const etcRoomId = computed<number | null>(() => {
  const etc = rooms.value.find(r => /기타/.test(r.label))
  return etc?.room ?? null
})

const isEtcSelected = computed(() => {
  return pendingRoom.value != null && etcRoomId.value != null && pendingRoom.value === etcRoomId.value
})

const pending = reactive({ c1: 0, c2: 0, s1: 0, s2: 0 })
const pendingDateTimeLabel = computed(() => {
  const day = pendingDayLabel.value || pendingDayKey.value
  return `${day} · ${timeLabel(pending.s1)} - ${timeLabel(pending.s2 + 1)}`
})

const canSave = computed(() => {
  if (!pendingDayKey.value) return false
  if (pendingRoom.value == null) return false
  if (!pickedCustomer.value?.customerId) return false
  if (isEtcSelected.value && !memo.value.trim()) return false
  return true
})

function canResize(ev: ScheduleEvent) {
  return canEditEvent(ev)
}

const resizing = ref<null | {
  id: string
  edge: 'start' | 'end'
  startY: number
  baseStart: number
  baseEnd: number
  moved: boolean
  snapshot: ScheduleEvent // 롤백용
}>(null)

function onResizeDown(e: PointerEvent, ev: ScheduleEvent, edge: 'start' | 'end') {
  if (!canResize(ev)) return

  resizing.value = {
    id: ev.id,
    edge,
    startY: e.clientY,
    baseStart: ev.startSlot,
    baseEnd: ev.endSlot,
    moved: false,
    snapshot: { ...ev }
  }

  window.addEventListener('pointermove', onResizeMove, { passive: true })
  window.addEventListener('pointerup', onResizeUp, { once: true })
}

function onResizeMove(e: PointerEvent) {
  const r = resizing.value
  if (!r) return

  const dy = e.clientY - r.startY
  const slotDelta = Math.round(dy / ROW_HEIGHT)
  if (Math.abs(dy) > 2) r.moved = true

  events.value = events.value.map(ev => {
    if (ev.id !== r.id) return ev

    const minDur = 1
    if (r.edge === 'end') {
      const nextEnd = clamp(r.baseEnd + slotDelta, r.baseStart + minDur, slotCount)
      return { ...ev, endSlot: nextEnd }
    } else {
      const nextStart = clamp(r.baseStart + slotDelta, 0, r.baseEnd - minDur)
      return { ...ev, startSlot: nextStart }
    }
  })
}

const suppressOpenModal = ref<{ id: string; until: number } | null>(null)
function isSuppressed(ev: ScheduleEvent) {
  const s = suppressOpenModal.value
  return !!(s && s.id === ev.id && Date.now() < s.until)
}
async function onResizeUp() {
  window.removeEventListener('pointermove', onResizeMove)

  const r = resizing.value
  resizing.value = null
  if (!r) return
  if (!r.moved) return
  suppressOpenModal.value = { id: r.id, until: Date.now() + 250 }

  const ev = events.value.find(x => x.id === r.id)
  if (!ev) return

  // 서버 반영 (실패하면 스냅샷으로 롤백)
  submitting.value = true
  try {
    const startAt = slotToLdt(ev.dayKey, ev.startSlot)
    const endAt = slotToLdt(ev.dayKey, ev.endSlot)

    const payload = {
      customerId: ev.customerId,
      roomId: ev.room,
      startAt,
      endAt,
      memo: ev.memo ?? null
    }

    await axios.put(`/api/work/visit/schedules/${ev.id}`, payload)
    await loadSchedules()
  } catch (err) {
    // 롤백
    events.value = events.value.map(x => (x.id === r.id ? r.snapshot : x))
  } finally {
    submitting.value = false
  }
}

const editingScheduledByName = ref('')
type VisitScheduleRow = {
  visitId: number
  customerId: number
  customerName: string
  customerPhone: string
  scheduledByUserId: number
  scheduledByName: string
  centerColor: string | null
  roomId: number | null
  roomName: string | null
  visitAt: string
  visitEndAt: string
  memo: string | null
}

type ScheduleEvent = {
  id: string
  title: string
  dayKey: string
  dayLabel: string
  room: number
  roomName?: string
  startSlot: number
  endSlot: number
  centerColor: string
  customerId: number
  customerName: string
  customerPhone: string
  memo?: string
  scheduledByUserId: number
  scheduledByName: string
}

function parseLocalDateTime(s: string) {
  if (!s) return null
  const iso = s.includes('T') ? s : s.replace(' ', 'T')
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? null : d
}

function slotOf(d: Date) {
  const m = d.getHours() * 60 + d.getMinutes()
  return Math.floor((m - START_MINUTES) / SLOT_MINUTES)
}

function slotCeilOf(d: Date) {
  const m = d.getHours() * 60 + d.getMinutes()
  return Math.ceil((m - START_MINUTES) / SLOT_MINUTES)
}

function slotToLdt(dayKey: string, slotIdx: number) {
  const totalMin = START_MINUTES + slotIdx * SLOT_MINUTES
  const hh = Math.floor(totalMin / 60)
  const mm = totalMin % 60
  return `${dayKey}T${pad2(hh)}:${pad2(mm)}:00`
}

function resolveRoomId(roomId: number | null) {
  if (roomId != null) return roomId
  const etc = rooms.value.find(r => r.label === '기타')
  return etc?.room ?? (roomListForGrid.value[0]?.room ?? 1)
}

function toEvent(r: VisitScheduleRow): ScheduleEvent | null {
  const startDt = parseLocalDateTime(r.visitAt)
  const endDt = parseLocalDateTime(r.visitEndAt)
  if (!startDt || !endDt) return null

  const dk = ymd(startDt)
  const dlabel = days.value.find(d => d.key === dk)?.label ?? dk

  const start = slotOf(startDt)
  const end = slotCeilOf(endDt)
  if (start < 0 || start >= slotCount) return null
  if (end <= start) return null

  return {
    id: String(r.visitId),
    title: r.scheduledByName || '',
    dayKey: dk,
    dayLabel: dlabel,
    room: resolveRoomId(r.roomId),
    roomName: r.roomName ?? undefined,
    startSlot: start,
    endSlot: Math.min(end, slotCount),
    centerColor: r.centerColor || '#64748B',
    customerId: r.customerId,
    customerName: r.customerName,
    customerPhone: r.customerPhone,
    memo: r.memo ?? '',
    scheduledByUserId: r.scheduledByUserId,
    scheduledByName: r.scheduledByName
  }
}

async function loadSchedules() {
  const from = ymd(weekStart.value)
  const to = ymd(addDays(weekStart.value, 7)) // exclusive
  const { data } = await axios.get('/api/work/visit/schedules', { params: { from, to } })
  const rows: VisitScheduleRow[] = Array.isArray(data) ? data : []
  events.value = rows.map(toEvent).filter(Boolean) as ScheduleEvent[]
}

function roomIndexOf(roomId: number) {
  return roomListForGrid.value.findIndex(r => r.room === roomId)
}
function dayRoomFromColIndex(colIdx: number) {
  const perDay = roomsPerDay.value
  const dayIdx = Math.floor(colIdx / perDay)
  const roomIdx = colIdx % perDay

  const dayKey = days.value[dayIdx]?.key
  const dayLabel = days.value[dayIdx]?.label
  const roomOpt = roomListForGrid.value[roomIdx] ?? roomListForGrid.value[0]
  return { dayKey, dayLabel, room: roomOpt.room }
}
function colIndexOf(dayKey: string, roomId: number) {
  const dayIdx = days.value.findIndex(d => d.key === dayKey)
  if (dayIdx < 0) return -1

  const roomIdx = roomIndexOf(roomId)
  if (roomIdx < 0) return -1

  return dayIdx * roomsPerDay.value + roomIdx
}

const events = ref<ScheduleEvent[]>([])

const visibleEvents = computed(() => {
  const daySet = new Set(days.value.map(d => d.key))
  return events.value.filter(e => daySet.has(e.dayKey))
})

function hexToRgba(hex: string, a: number) {
  const h = (hex || '').replace('#', '')
  const v = h.length === 3 ? h.split('').map(x => x + x).join('') : h
  const r = parseInt(v.slice(0, 2), 16)
  const g = parseInt(v.slice(2, 4), 16)
  const b = parseInt(v.slice(4, 6), 16)
  if (![r, g, b].every(Number.isFinite)) return `rgba(100,116,139,${a})`
  return `rgba(${r},${g},${b},${a})`
}

function eventClass(_ev: ScheduleEvent) {
  return 'border text-gray-900 dark:text-gray-100'
}

function eventStyle(ev: ScheduleEvent) {
  const colIdx = colIndexOf(ev.dayKey, ev.room)
  const left = colIdx * COL_WIDTH
  const top = ev.startSlot * ROW_HEIGHT
  const height = Math.max(ROW_HEIGHT, (ev.endSlot - ev.startSlot) * ROW_HEIGHT)

  return {
    left: `${left}px`,
    top: `${top}px`,
    width: `${COL_WIDTH}px`,
    height: `${height}px`,
    padding: '2px',
    cursor: canEditEvent(ev)
        ? (dragging.value?.id === ev.id ? 'grabbing' : 'grab')
        : 'default',
    backgroundColor: hexToRgba(ev.centerColor, 0.10),
    borderColor: hexToRgba(ev.centerColor, 0.45),
  } as any
}

/** ===== 모달(추가/수정) ===== */
const isOpen = ref(false)
const editingEventId = ref<string | null>(null)

const pendingStartLabel = computed(() => timeLabel(pending.s1))
const pendingEndLabel = computed(() => timeLabel(pending.s2 + 1))
const pendingColsLabel = computed(() => {
  const cols: string[] = []
  for (let c = pending.c1; c <= pending.c2; c++) {
    const { dayLabel, room } = dayRoomFromColIndex(c)
    cols.push(`${dayLabel} ${room}룸`)
  }
  return cols.join(', ')
})
const selectionSummary = computed(() => {
  if (editingEventId.value) return '드래그로 이동 가능, 여기서 제목 수정 및 삭제 가능'
  return '드래그로 선택한 구간과 열 전체에 동일 일정이 생성됨'
})

function closeModal() {
  if (pendingDragEdit.value && pendingDragEdit.value.id === editingEventId.value) {
    const snap = pendingDragEdit.value.snapshot
    events.value = events.value.map(x => (x.id === snap.id ? snap : x))
    pendingDragEdit.value = null
  }

  isOpen.value = false
  editingEventId.value = null
  editingScheduledByName.value = ''
  memo.value = ''

  customerQuery.value = ''
  customerResults.value = []

  if (!isCustomerLocked.value) pickedCustomer.value = null

  clearSelection()
}

function openAddFromSelection() {
  const { c1, c2, s1, s2 } = normalizeSelection()
  if (c1 < 0 || s1 < 0) return

  pending.c1 = c1
  pending.c2 = c2
  pending.s1 = s1
  pending.s2 = s2

  editingEventId.value = null
  isOpen.value = true
}

function openManualAdd() {
  pending.c1 = 0
  pending.c2 = 0
  pending.s1 = 2
  pending.s2 = 2
  editingEventId.value = null
  editingScheduledByName.value = myUserName.value
  isOpen.value = true
}

async function openEditEvent(ev: any) {
  pendingDayKey.value = ev.dayKey
  pendingDayLabel.value = ev.dayLabel
  pendingSlot.value = ev.startSlot
  pendingRoom.value = ev.room
  memo.value = ev.memo ?? ''
  editingEventId.value = ev.id
  editingScheduledByName.value = ev.scheduledByName ?? ''

  const col = colIndexOf(ev.dayKey, ev.room)
  pending.c1 = col >= 0 ? col : 0
  pending.c2 = pending.c1
  pending.s1 = ev.startSlot
  pending.s2 = Math.max(ev.startSlot, (ev.endSlot ?? ev.startSlot + 1) - 1)

  editingEventId.value = ev.id

  if (ev.customerId) {
    pickedCustomer.value = {
      customerId: ev.customerId,
      customerName: ev.customerName ?? '',
      customerPhone: ev.customerPhone ?? '',
      customerCreatedAt: ev.customerCreatedAt ?? ''
    }
  }

  await loadRooms()

  if (isCustomerLocked.value) {
    await loadLockedCustomer()
  }

  isOpen.value = true
  await nextTick()
  modalRoot.value?.focus?.()
}
async function openModalFromSelection(range: { c1: number; c2: number; s1: number; s2: number }) {
  const { c1, c2, s1, s2 } = range
  if (c1 < 0 || s1 < 0) return

  // 선택 범위 저장
  pending.c1 = c1
  pending.c2 = c2
  pending.s1 = s1
  pending.s2 = s2

  // 모달 표시용(시작셀 기준)
  const { dayKey, dayLabel, room } = dayRoomFromColIndex(c1)
  pendingDayKey.value = dayKey || ''
  pendingDayLabel.value = dayLabel || ''
  pendingSlot.value = s1
  pendingRoom.value = room

  await loadRooms()

  if (isCustomerLocked.value) {
    await loadLockedCustomer()
    const exist = events.value.find(e => (e as any).customerId === lockedCustomerId.value)
    if (exist) {
      await openEditEvent(exist as any)
      return
    }
  }

  editingEventId.value = null
  editingScheduledByName.value = myUserName.value
  isOpen.value = true
  await nextTick()
  modalRoot.value?.focus?.()
}

async function saveEvent() {
  if (!editingEventId.value && !canWrite.value) return
  if (editingEventId.value && !modalCanEdit.value) return
  if (!canSave.value) return

  submitting.value = true
  try {
    const startAt = slotToLdt(pendingDayKey.value, pending.s1)
    const endAt = slotToLdt(pendingDayKey.value, pending.s2 + 1)

    const payload = {
      customerId: pickedCustomer.value!.customerId,
      roomId: pendingRoom.value!,
      startAt,
      endAt,
      memo: memo.value.trim() || null
    }

    if (editingEventId.value) {
      await axios.put(`/api/work/visit/schedules/${editingEventId.value}`, payload)
      pendingDragEdit.value = null
    } else {
      await axios.post(`/api/work/visit/schedules`, payload)
    }

    await loadSchedules()
    closeModal()
  } finally {
    submitting.value = false
  }
}

async function deleteEvent(id: string | null) {
  if (!id) return
  const ev = events.value.find(e => e.id === id)
  if (!ev || !canEditEvent(ev)) return

  submitting.value = true
  try {
    await axios.delete(`/api/work/visit/schedules/${id}`)
    await loadSchedules()
    if (editingEventId.value === id) closeModal()
  } finally {
    submitting.value = false
  }
}

/** ===== 이벤트 드래그 이동 ===== */
const dragging = ref<null | {
  id: string
  startX: number
  startY: number
  baseCol: number
  baseStart: number
  baseEnd: number
  moved: boolean
  snapshot: ScheduleEvent
  autoVx: number
}>(null)

function clamp(n: number, min: number, max: number) { return Math.max(min, Math.min(max, n)) }

function onEventPointerDown(e: PointerEvent, ev: ScheduleEvent) {
  if (!canEditEvent(ev)) return
  const col = colIndexOf(ev.dayKey, ev.room)
  dragging.value = {
    id: ev.id,
    startX: e.clientX,
    startY: e.clientY,
    baseCol: col,
    baseStart: ev.startSlot,
    baseEnd: ev.endSlot,
    moved: false,
    snapshot: { ...ev },
    autoVx: 0,
  }

  try { (e.currentTarget as HTMLElement)?.setPointerCapture?.(e.pointerId) } catch {}

  window.addEventListener('pointermove', onEventPointerMove, { passive: true })
  window.addEventListener('pointerup', onEventPointerUp, { once: true })
}

function autoScrollWhileDraggingX(clientX: number, d: { autoVx: number }) {
  const host = scrollHost.value
  if (!host) return 0

  const rect = host.getBoundingClientRect()

  let dir = 0
  let t = 0 // 0..1 (엣지에 가까울수록 1)

  const leftEdge = rect.left + AUTO_SCROLL_MARGIN
  const rightEdge = rect.right - AUTO_SCROLL_MARGIN

  if (clientX < leftEdge) {
    dir = -1
    t = (leftEdge - clientX) / AUTO_SCROLL_MARGIN
  } else if (clientX > rightEdge) {
    dir = 1
    t = (clientX - rightEdge) / AUTO_SCROLL_MARGIN
  } else {
    // 엣지 밖이면 속도 0으로 수렴
    d.autoVx += (0 - d.autoVx) * AUTO_SCROLL_EASE
    if (Math.abs(d.autoVx) < 0.05) d.autoVx = 0
    return 0
  }

  t = clamp(t, 0, 1)

  const targetV = dir * (AUTO_SCROLL_MIN + (AUTO_SCROLL_MAX - AUTO_SCROLL_MIN) * t)

  // 속도 스무딩(저역통과)
  d.autoVx += (targetV - d.autoVx) * AUTO_SCROLL_EASE

  const before = host.scrollLeft
  host.scrollLeft = before + d.autoVx

  return host.scrollLeft - before // 실제로 움직인 값(경계에 막히면 줄어듦)
}
function onEventPointerMove(e: PointerEvent) {
  const d = dragging.value
  if (!d) return

  // 1) 스크롤 먼저(스무딩됨)
  const scrolled = autoScrollWhileDraggingX(e.clientX, d)

  // 2) 스크롤로 content가 움직인 만큼 startX를 보정 (이게 핵심)
  if (scrolled) d.startX -= scrolled

  // 3) 이제 dx/dy 계산
  const dx = e.clientX - d.startX
  const dy = e.clientY - d.startY

  if (Math.abs(dx) + Math.abs(dy) > 2) d.moved = true

  const colDelta = Math.round(dx / COL_WIDTH)
  const slotDelta = Math.round(dy / ROW_HEIGHT)

  const duration = d.baseEnd - d.baseStart
  const nextCol = clamp(d.baseCol + colDelta, 0, colCount.value - 1)
  const nextStart = clamp(d.baseStart + slotDelta, 0, slotCount - duration)
  const nextEnd = nextStart + duration

  events.value = events.value.map(ev => {
    if (ev.id !== d.id) return ev
    const { dayKey, dayLabel, room } = dayRoomFromColIndex(nextCol)
    return {
      ...ev,
      dayKey: dayKey || ev.dayKey,
      dayLabel: dayLabel || ev.dayLabel,
      room,
      startSlot: nextStart,
      endSlot: nextEnd
    }
  })
}

const pendingDragEdit = ref<null | { id: string; snapshot: ScheduleEvent }>(null)
async function onEventPointerUp() {
  window.removeEventListener('pointermove', onEventPointerMove)

  const d = dragging.value
  // click-열기 방지용 플래그는 onEventClick에서 확인하니까, 여기서는 바로 null로 두면 안됨
  // (moved 판정 후 setTimeout으로 비워야 함)
  if (!d) return

  // 드래그 안 했으면 종료
  if (!d.moved) {
    setTimeout(() => { dragging.value = null }, 0)
    return
  }

  // 드래그한 결과 좌표로 서버 저장
  const ev = events.value.find(x => x.id === d.id)
  if (!ev) {
    setTimeout(() => { dragging.value = null }, 0)
    return
  }

  // 내 이벤트 아니면 즉시 롤백 + (모달 종료)
  if (!canEditEvent(ev)) {
    events.value = events.value.map(x => (x.id === d.id ? d.snapshot : x))
    setTimeout(() => { dragging.value = null }, 0)
    return
  }

  const etcId = etcRoomId.value
  const isEtcRoom = etcId != null && ev.room === etcId
  const hasMemo = (ev.memo ?? '').trim().length > 0

  if (isEtcRoom && !hasMemo) {
    pendingDragEdit.value = { id: d.id, snapshot: d.snapshot }

    await openEditEvent(ev)

    setTimeout(() => { dragging.value = null }, 0)
    return
  }

  submitting.value = true
  try {
    const startAt = slotToLdt(ev.dayKey, ev.startSlot)
    const endAt = slotToLdt(ev.dayKey, ev.endSlot)

    const payload = {
      customerId: ev.customerId,
      roomId: ev.room,
      startAt,
      endAt,
      memo: (ev.memo ?? '').trim() || null
    }

    await axios.put(`/api/work/visit/schedules/${ev.id}`, payload)

    // 서버값으로 다시 맞추기
    await loadSchedules()
  } catch (err) {
    // 실패하면 원복
    events.value = events.value.map(x => (x.id === d.id ? d.snapshot : x))
  } finally {
    submitting.value = false
    setTimeout(() => { dragging.value = null }, 0)
  }
}

function onEventClick(ev: ScheduleEvent) {
  if (isSuppressed(ev)) return
  // 드래그 직후 click가 따라오는 케이스 방지
  if (dragging.value?.id === ev.id && dragging.value?.moved) return
  openEditEvent(ev)
}

/** ===== 주 이동 ===== */
function goPrevWeek() { weekStart.value = addDays(weekStart.value, -7) }
function goNextWeek() { weekStart.value = addDays(weekStart.value, 7) }
function goToday() {
  todayKey.value = ymd(new Date())
  weekStart.value = startOfWeek(new Date())
}

/** ====== api호출 ===== */
async function loadRooms() {
  try {
    const resp = await axios.get('/api/work/visit/rooms')
    const data = resp.data

    // console.log('rooms status =', resp.status)
    // console.log('rooms api data =', data)

    const arr =
        Array.isArray(data) ? data
            : Array.isArray((data as any)?.rooms) ? (data as any).rooms
                : Array.isArray((data as any)?.items) ? (data as any).items
                    : []

    rooms.value = arr
        .filter((r: any) => !!r)
        // 서버가 이미 active만 주는 게 정상이라 isActive로 거르지 말고 일단 다 받는 게 안전함
        .map((r: any) => {
          const room = Number(
              r.roomId ?? r.id ?? r.room ?? r.room_id ?? r.roomID ?? r.roomNo
          )
          const label = String(
              r.roomName ?? r.name ?? r.label ?? r.room_name ?? r.roomNM ?? ''
          ).trim()

          return { room, label }
        })
        .filter((r: any) => Number.isFinite(r.room) && r.room > 0 && r.label.length > 0)

    // console.log('rooms parsed =', rooms.value)
  } catch (e: any) {
    console.error('loadRooms failed', e)
    console.error('status =', e?.response?.status)
    console.error('data =', e?.response?.data)
    rooms.value = []
  }
}

async function loadLockedCustomer() {
  if (!isCustomerLocked.value) return
  if (pickedCustomer.value?.customerId === lockedCustomerId.value) return

  try {
    const { data } = await axios.get(`/api/work/visit/customers/${lockedCustomerId.value}`)
    pickedCustomer.value = {
      customerId: data.customerId,
      customerName: data.customerName,
      customerPhone: data.customerPhone,
      customerCreatedAt: data.customerCreatedAt
    }
  } catch (e) {
    console.error(e)
  }
}

function mapCustomer(c: any): CustomerPick {
  return {
    customerId: Number(c.customerId ?? c.id),
    customerName: String(c.customerName ?? c.name ?? ''),
    customerPhone: String(c.customerPhone ?? c.phone ?? ''),
    customerCreatedAt: String(c.customerCreatedAt ?? c.createdAt ?? '')
  }
}

async function searchCustomers() {
  try {
    if (!customerQuery.value) {
      customerResults.value = []
      return
    }
    const { data } = await axios.get('/api/work/visit/customers', {
      params: { keyword: customerQuery.value }
    })
    customerResults.value = (Array.isArray(data) ? data : []).map(mapCustomer)
  } catch (e) {
    console.error(e)
  }
}

type VisitFocusResp = {
  visitId: number
  customerId: number
  roomId: number
  visitAt: string
  visitEndAt: string
}

async function loadFocusIfLocked() {
  if (!lockedCustomerId.value) return

  try {
    const { data } = await axios.get<VisitFocusResp>('/api/work/visit/schedules/focus', {
      params: { customerId: lockedCustomerId.value }
    })

    const dt = parseLocalDateTime(data.visitAt)
    if (dt) {
      focusDayKey.value = ymd(dt)
      weekStart.value = startOfWeek(dt)   // 여기서 주가 바뀜
      shouldAutoOpenFocus.value = true
    }
  } catch {
    // 예약이 없으면(404) 그냥 현재 주 유지
    focusDayKey.value = null
    shouldAutoOpenFocus.value = false
  }
}

let ct: any = null
function debouncedCustomerSearch() {
  clearTimeout(ct)
  ct = setTimeout(searchCustomers, 250)
}

function selectCustomer(c: CustomerPick) {
  pickedCustomer.value = c
}

function clearPickedCustomer() {
  if (isCustomerLocked.value) return
  pickedCustomer.value = null
}

// 새로고침
const loadingSchedules = ref(false)
async function onRefreshSchedules() {
  if (loadingSchedules.value) return
  loadingSchedules.value = true
  try {
    await loadRooms()      // 룸 변경 가능성 있으면 같이
    await loadSchedules()  // 주간 일정 다시 로드
  } finally {
    loadingSchedules.value = false
  }
}
</script>
