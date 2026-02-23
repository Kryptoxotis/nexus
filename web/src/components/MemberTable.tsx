'use client'

import type { BusinessMember } from '@/lib/types'

interface MemberTableProps {
  members: (BusinessMember & { profile?: { display_name: string | null; email: string | null } })[]
}

export default function MemberTable({ members }: MemberTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="border-b border-gray-200">
            <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Member</th>
            <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Role</th>
            <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Status</th>
            <th className="text-left py-3 px-4 text-sm font-medium text-gray-500">Joined</th>
          </tr>
        </thead>
        <tbody>
          {members.map((member) => (
            <tr key={member.id} className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-3 px-4">
                <div>
                  <p className="text-sm font-medium text-gray-900">
                    {member.profile?.display_name || 'Unknown'}
                  </p>
                  <p className="text-xs text-gray-500">{member.profile?.email}</p>
                </div>
              </td>
              <td className="py-3 px-4">
                <span className={`px-2 py-0.5 text-xs font-medium rounded-full capitalize ${
                  member.role === 'owner' ? 'bg-purple-100 text-purple-800' :
                  member.role === 'admin' ? 'bg-blue-100 text-blue-800' :
                  'bg-gray-100 text-gray-600'
                }`}>
                  {member.role}
                </span>
              </td>
              <td className="py-3 px-4">
                <span className={`px-2 py-0.5 text-xs font-medium rounded-full capitalize ${
                  member.status === 'active' ? 'bg-green-100 text-green-800' :
                  member.status === 'pending' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-600'
                }`}>
                  {member.status}
                </span>
              </td>
              <td className="py-3 px-4 text-sm text-gray-500">
                {new Date(member.joined_at).toLocaleDateString()}
              </td>
            </tr>
          ))}
          {members.length === 0 && (
            <tr>
              <td colSpan={4} className="py-8 text-center text-sm text-gray-500">
                No members yet
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
