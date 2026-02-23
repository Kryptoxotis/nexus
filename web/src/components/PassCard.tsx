'use client'

import type { Pass } from '@/lib/types'

interface PassCardProps {
  pass: Pass
  onDelete?: (id: string) => void
}

export default function PassCard({ pass, onDelete }: PassCardProps) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-sm transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <h3 className="font-semibold text-gray-900">{pass.pass_name}</h3>
          <p className="text-sm text-gray-600 mt-0.5">{pass.organization}</p>
          <p className="text-xs text-gray-400 mt-1">ID: {pass.pass_id}</p>
          {pass.link && (
            <a href={pass.link} target="_blank" rel="noopener noreferrer" className="text-xs text-blue-600 hover:underline mt-1 block">
              {pass.link}
            </a>
          )}
          {pass.expiry_date && (
            <p className="text-xs text-gray-400 mt-1">Expires: {pass.expiry_date}</p>
          )}
        </div>
        <div className="flex items-center gap-2">
          {pass.is_active && (
            <span className="px-2 py-1 text-xs font-medium rounded-full bg-green-100 text-green-800">
              Active
            </span>
          )}
          {onDelete && (
            <button
              onClick={() => onDelete(pass.id)}
              className="p-1.5 text-gray-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition-colors"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
